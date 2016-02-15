package net.minecrell.gradle.licenser.header

import net.minecrell.gradle.licenser.util.HeaderHelper

class Header {

    final HeaderFormatRegistry registry
    final List<String> keywords

    private final Closure<String> loader
    final boolean newLine
    private String text

    private final Map<HeaderFormat, PreparedHeader> formatted = new HashMap<>()

    Header(HeaderFormatRegistry registry, List<String> keywords, Closure<String> loader, boolean newLine) {
        this.registry = registry
        this.keywords = keywords*.toLowerCase().asImmutable()
        this.loader = loader
        this.newLine = newLine
    }

    String getText() {
        if (this.text == null) {
            this.text = loader.call()
            if (!containsKeyword(this.text)) {
                throw new IllegalArgumentException("Header does not contain any of the required keywords: $keywords")
            }
        }

        return this.text
    }

    boolean containsKeyword(String s) {
        s = s.toLowerCase()
        return keywords.any(s.&contains)
    }

    PreparedHeader prepare(HeaderFormat format) {
        if (format == null) {
            return null
        }

        PreparedHeader result = formatted[format]
        if (result == null) {
            result = format.prepare(this, getText())
            formatted[format] = result
        }
        return result
    }

    PreparedHeader prepare(String path) {
        return prepare(registry[HeaderHelper.getExtension(path)])
    }

    PreparedHeader prepare(File file) {
        return prepare(file.path)
    }

}
