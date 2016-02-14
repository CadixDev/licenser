package net.minecrell.gradle.licenser.header

enum StandardHeaderFormat {
    SLASH_STAR('/*', '*/', '/*', ' *', ' */', 'java', 'groovy', 'scala', 'gradle')

    private final CommentHeaderFormat format
    private final String[] extensions

    StandardHeaderFormat(String start, String end, String firstLine, String prefix, String lastLine, String... extensions) {
        this.format = new CommentHeaderFormat(this.name(), start, end, firstLine, prefix, lastLine)
        this.extensions = extensions
    }

    CommentHeaderFormat getFormat() {
        return format
    }

    static void register() {
        for (StandardHeaderFormat format : values()) {
            format.extensions.each { HeaderFormats.register(format.format, it) }
        }
    }

}
