package net.minecrell.gradle.licenser.header

enum HeaderStyle {
    SLASH_STAR('/*', '*/', '/*', ' *', ' */', 'java', 'groovy', 'scala', 'gradle'),
    JAVADOC('/**', '*/', '/**', ' *', ' */')

    private final CommentHeaderFormat format
    private final String[] extensions

    HeaderStyle(String start, String end, String firstLine, String prefix, String lastLine, String... extensions) {
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
