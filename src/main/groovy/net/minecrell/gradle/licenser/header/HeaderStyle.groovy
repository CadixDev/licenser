package net.minecrell.gradle.licenser.header

import java.util.regex.Pattern

enum HeaderStyle {
    BLOCK_COMMENT(~/^\s*\/\*(?:[^*].*)?$/, ~/\*\/\s*(.*?)$/, null, '/*', ' *', ' */', 'java', 'groovy', 'scala', 'gradle', 'css', 'js'),
    JAVADOC(~/^\s*\/\*\*(?:[^*].*)?$/, ~/\*\/\s*(.*?)$/, null, '/**', ' *', ' */'),
    HASH(~/^\s*#/, null, ~/^\s*#!/, '#', '#', '#', 'properties', 'yml', 'yaml', 'sh'),
    XML(~/^\s*<!--/, ~/-->\s*(.*?)$/, ~/^\s*<(?:\?xml .*\?|!DOCTYPE .*)>\s*$/, '<!--', ' ', '-->', 'xml', 'xsd', 'xsl', 'fxml', 'dtd', 'html', 'xhtml')

    final CommentHeaderFormat format
    private final String[] extensions

    HeaderStyle(Pattern start, Pattern end, Pattern skipLine, String firstLine, String prefix, String lastLine, String... extensions) {
        this.format = new CommentHeaderFormat(this.name(), start, end, skipLine, firstLine, prefix, lastLine)
        this.extensions = extensions
    }

    static void register() {
        for (HeaderStyle style : values()) {
            style.extensions.each { HeaderFormats.register(style.format, it) }
        }
    }

}
