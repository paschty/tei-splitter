package org.mycore.tei;

/**
 * Interface which provides the name for a splitted of part of a tei file.
 */
public interface TEIFileNameGenerator {
    /**
     *
     * @param originalFileName the name of the {@link TEIFile} where this file is splitted of
     * @param facs the fac attribute of the pb element if present
     * @param n the n attribute of the pb element if present
     * @return the generated name of the new tei file
     */
    String generateName(final String originalFileName, final String facs, final String n);
}
