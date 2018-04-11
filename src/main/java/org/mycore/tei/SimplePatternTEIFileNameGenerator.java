package org.mycore.tei;

import java.util.Objects;

public class SimplePatternTEIFileNameGenerator implements TEIFileNameGenerator {

    public SimplePatternTEIFileNameGenerator(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    private String pattern;

    @Override
    public String generateName(String originalFileNameParam, String facsParam, String nParam) {
        final String n = (Objects.isNull(nParam)) ? "" : nParam;
        final String originalName = (Objects.isNull(originalFileNameParam)) ? "" : originalFileNameParam;
        final String facs = (Objects.isNull(facsParam)) ? "" : facsParam;
        return this.pattern
            .replaceAll("%n%", n)
            .replaceAll("%name%", originalName)
            .replaceAll("%facs%", facs);
    }
}
