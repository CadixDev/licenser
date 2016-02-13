package net.minecrell.gradle.licenser.util;

public final class StringHelper {

    private StringHelper() {
    }

    public static String trimEnd(String s) {
        final int last = s.length() - 1;
        for (int i = last; i >= 0; i--) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return i == last ? s : s.substring(0, i + 1);
            }
        }

        return "";
    }

    public static String getExtension(String s) {
        final int pos = s.lastIndexOf('.');
        return pos >= 0 ? s.substring(pos + 1) : null;
    }

}
