package net.minecrell.gradle.licenser.header

final class HeaderFormats {

    private HeaderFormats() {
    }

    static final HeaderFormat SLASHSTAR = new CommentHeaderFormat('SLASHSTAR', '/*', ' *', ' */')

    static {
        register(SLASHSTAR, 'java', 'groovy', 'scala', 'gradle')
    }

    private static final Map<String, HeaderFormat> formats = new HashMap<>()

    static HeaderFormat register(HeaderFormat format, String extension) {
        extension = extension.toLowerCase(Locale.ROOT)

        if (format == null) {
            return formats.remove(extension)
        } else {
            return formats.put(extension, format)
        }
    }

    static void register(HeaderFormat format, String... extensions) {
        extensions.each { register(format, it) }
    }

    static HeaderFormat find(String extension) {
        return extension != null ? formats[extension.toLowerCase(Locale.ROOT)] : null
    }

}
