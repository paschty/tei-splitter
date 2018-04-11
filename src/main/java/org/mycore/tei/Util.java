package org.mycore.tei;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMSource;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.xml.sax.SAXException;

public class Util {

    public static final Namespace TEI_NS = Namespace.getNamespace("tei", "http://www.tei-c.org/ns/1.0");

    public static void main(String[] args) throws IOException {
        if(args.length==2){
            final URL uri = new URL(args[0]);
            final String dtaURLPattern = "http://media.dwds.de/dta/images/%dirName%/%dirName%_%fileName%_800px.jpg";

            try(final InputStream inputStream = uri.openStream()){
                final Document document = new SAXBuilder().build(inputStream);
                final String dirName = XPathFactory.instance().compile("//tei:idno[@type='DTADirName']/text()",
                    Filters.text(), null, Util.TEI_NS).evaluateFirst(document).getText();
                final Path baseFolder = FileSystems.getDefault().getPath(args[1] + dirName);
                Files.createDirectories(baseFolder.resolve("tei/transcription"));
                final SimpleTEIFile stf = new SimpleTEIFile("file", document);
                final TEISplitter teiSplitter = new TEISplitter(stf,
                    new TEIFileNameGenerator() {
                        @Override
                        public String generateName(String originalFileNameParam, String facsParam, String nParam) {

                            final String fileName = facsParam.replaceAll("#f", "");

                            download(dtaURLPattern.replaceAll("%dirName%", dirName).replaceAll("%fileName%",fileName), baseFolder.resolve(fileName+".jpg"));

                            return fileName;
                        }

                        private void download(String url, Path to){
                            try {
                                final URL url1 = new URL(url);
                                try(InputStream i = url1.openStream()){
                                    Files.copy(i, to, StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                teiSplitter.split().forEach(teiFile -> {
                    try(final OutputStream os = Files.newOutputStream(baseFolder.resolve("tei/transcription/"+ teiFile.getName().replaceAll("#", "")+".xml"))) {
                        new XMLOutputter().output(teiFile.getDocument(), os);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            } catch (JDOMException e) {
                e.printStackTrace();
            }

        }
    }
}