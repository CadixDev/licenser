package net.minecrell.gradle.licenser.header

import java.util.regex.Pattern

enum HeaderStyle {
    SLASH_STAR(~/^\s*\/\*(?:[^*].*)?$/, ~/\*\/\s*(.*?)$/, '/*', ' *', ' */', 'java', 'groovy', 'scala', 'gradle'),
    JAVADOC(~/^\s*\/\*\*(?:[^*].*)?$/, ~/\*\/\s*(.*?)$/, '/**', ' *', ' */')

    private final CommentHeaderFormat format
    private final String[] extensions

    HeaderStyle(Pattern start, Pattern end, String firstLine, String prefix, String lastLine, String... extensions) {
        this.format = new CommentHeaderFormat(this.name(), start, end, firstLine, prefix, lastLine)
        this.extensions = extensions
    }

    CommentHeaderFormat getFormat() {
        return format
    }

    List<String> getExtensions() {
        return this.extensions.toList()
    }

    static void register() {
        for (HeaderStyle style : values()) {
            style.extensions.each { HeaderFormats.register(style.format, it) }
        }
    }

}
