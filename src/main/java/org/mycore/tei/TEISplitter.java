package org.mycore.tei;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

/**
 * Can be used to Split one TEI file into small simple files.
 */
public class TEISplitter {

    //private static Logger LOGGER = LogManager.getLogger();

    private TEIFile original;

    private Element copyTarget;

    private List<TEIFile> splitDocumentList = new ArrayList<>();

    private TEIFileNameGenerator nameGenerator;

    private int size = -1;

    /**
     * Creates a new TEI-Splitter
     * @param original the original TEI files.
     * @param nameGenerator provides the names for the subfiles
     */
    public TEISplitter(TEIFile original, TEIFileNameGenerator nameGenerator) {
        this.original = original;
        this.nameGenerator = nameGenerator;
    }

    /**
     * @return true if the file contains a pb and is splittable
     */
    public boolean isSplitable() {
        XPathFactory xFactory = XPathFactory.instance();
        XPathExpression<Element> expr = xFactory.compile("//tei:pb", Filters.element(), null, Util.TEI_NS);
        List<Element> elementList = expr.evaluate(this.original.getDocument());
        return elementList.size() > 0;
    }

    /**
     * @return the count of pb elements (the count of created files!)
     */
    public int getEstimatedSize() {
        if (size == -1) {
            XPathFactory xFactory = XPathFactory.instance();
            XPathExpression<Element> expr = xFactory.compile("//tei:pb", Filters.element(), null, Util.TEI_NS);
            List<Element> elementList = expr.evaluate(this.original.getDocument());
            size = elementList.size();
        }

        return size;
    }

    private TEIFile newStub(final String nParam, final String facs) {
        Document clone = original.getDocument().clone();
        clone.getRootElement().removeChild("teiHeader", Util.TEI_NS);
        Element child = clone.getRootElement().getChild("text", Util.TEI_NS);
        child.removeContent();
        this.copyTarget = child;

        TEIFile newTEIFile = new SimpleTEIFile(nameGenerator.generateName(original.getName(), facs, nParam), clone);
        this.splitDocumentList.add(newTEIFile);
        return newTEIFile;
    }

    private void copyAncestors(Element pbElement) {
        Element parent = pbElement;
        Element lastClone = null;
        Element body = this.copyTarget;
        while (!(parent = parent.getParentElement()).getName().equals("text")) {
            Element cloned = cloneElement(parent);
            if (lastClone != null) {
                cloned.addContent(lastClone);
            } else {
                // first cloned
                //this.copyTarget.addContent(cloned);
                this.copyTarget = cloned;

            }
            lastClone = cloned;
        }

        if (lastClone != null) {
            body.addContent(lastClone);
        }
    }

    private void traverse(Element element) {
        for (Content content : element.getContent()) {
            if (content instanceof Element) {
                Element contentElement = (Element) content;
                if (contentElement.getName().equals("pb")) {
                    newStub(
                        contentElement.getAttributeValue("n"),
                        contentElement.getAttributeValue("facs")
                    );
                    copyAncestors(contentElement);
                    continue;
                }
            }

            copyToNew(content);

        }

        copyTarget = copyTarget.getParentElement();

    }

    private void copyToNew(Content content) {
        if (content instanceof Element) {
            Element elementContent = (Element) content;

            Element cloned = cloneElement(elementContent);
            copyTarget.addContent(cloned);

            copyTarget = cloned;
            traverse(elementContent);
        } else {
            copyTarget.addContent(content.clone());
        }

    }

    private Element cloneElement(Element elementContent) {
        Element element = new Element(elementContent.getName(), elementContent.getNamespace());
        elementContent.getAttributes()
            .stream()
            .map(Attribute::clone)
            .forEach(element::setAttribute);

        return element;
    }

    /**
     * Splits a tei file.
     * @return a list of all (sub-)files
     */
    public List<TEIFile> split() {
        if (splitDocumentList.size() > 0) {
            splitDocumentList = new ArrayList<>();
        }

        Document clone = original.getDocument().clone();
        clone.getRootElement().removeChild("teiHeader", Util.TEI_NS);
        Element child = clone.getRootElement().getChild("text", Util.TEI_NS);
        child.removeContent();
        this.copyTarget = child;

        Element text = this.original.getDocument().getRootElement().getChild("text", Util.TEI_NS);
        traverse(text);
        return splitDocumentList;
    }

}