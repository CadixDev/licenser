package net.minecrell.gradle.licenser.header

import net.minecrell.gradle.licenser.util.StringHelper

class Header {

    private final Closure<String> loader
    private String text
    private final boolean newLine

    private final Map<HeaderFormat, PreparedHeader> formatted = new HashMap<>()

    Header(Closure<String> loader, boolean newLine) {
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
        return prepare(HeaderFormats.find(StringHelper.getExtension(path)))
    }

    PreparedHeader prepare(File file) {
        return prepare(file.path)
    }

}
