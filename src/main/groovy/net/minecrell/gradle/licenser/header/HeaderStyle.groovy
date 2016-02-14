package net.minecrell.gradle.licenser.header

import java.util.regex.Pattern

enum HeaderStyle {
    BLOCK_COMMENT(~/^\s*\/\*(?:[^*].*)?$/, ~/\*\/\s*(.*?)$/, '/*', ' *', ' */', 'java', 'groovy', 'scala', 'gradle'),
    JAVADOC(~/^\s*\/\*\*(?:[^*].*)?$/, ~/\*\/\s*(.*?)$/, '/**', ' *', ' */'),
    HASH(~/^\s*#/, null, '#', '#', '#', 'properties', 'yml', 'yaml')

    final CommentHeaderFormat format
    private final String[] extensions

    HeaderStyle(Pattern start, Pattern end, String firstLine, String prefix, String lastLine, String... extensions) {
        this.format = new CommentHeaderFormat(this.name(), start, end, firstLine, prefix, lastLine)
        this.extensions = extensions
    }

    static void register() {
        for (HeaderStyle style : values()) {
            style.extensions.each { HeaderFormats.register(style.format, it) }
        }
    }

}
