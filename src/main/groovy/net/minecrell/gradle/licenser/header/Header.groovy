package net.minecrell.gradle.licenser.header

import net.minecrell.gradle.licenser.util.HeaderHelper

class Header {

    private final HeaderFormatRegistry registry
    private final Closure<String> loader
    private final boolean newLine
    private String text

    private final Map<HeaderFormat, PreparedHeader> formatted = new HashMap<>()

    Header(HeaderFormatRegistry registry, Closure<String> loader, boolean newLine) {
        this.registry = registry
        this.loader = loader
        this.newLine = newLine
    }

    String getText() {
        if (this.text == null) {
            this.text = loader.call()
        }

        return this.text
    }

    PreparedHeader prepare(HeaderFormat format) {
        if (format == null) {
            return null
        }

        PreparedHeader result = formatted[format]
        if (result == null) {
            result = format.prepare(getText(), this.newLine)
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
