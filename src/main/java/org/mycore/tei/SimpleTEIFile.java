package org.mycore.tei;

import org.jdom2.Document;

/**
 * Simple "in memory" TEI file
 */
public class SimpleTEIFile implements TEIFile {

    private final String name;
    private final Document document;

    public SimpleTEIFile(String name, Document document) {
        this.name = name;
        this.document = document;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Document getDocument() {
        return this.document;
    }
}
