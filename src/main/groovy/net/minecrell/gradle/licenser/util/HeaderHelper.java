package net.minecrell.gradle.licenser.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Pattern;

public final class HeaderHelper {

    private HeaderHelper() {
    }

    public static String getExtension(String s) {
        final int pos = s.lastIndexOf('.');
        return pos >= 0 ? s.substring(pos + 1) : null;
    }

    public static String stripIndent(String s) {
        final int len = s.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return i == 0 ? s : s.substring(i);
            }
        }

        return "";
    }

    public static String stripTrailingIndent(String s) {
        final int last = s.length() - 1;
        for (int i = last; i >= 0; i--) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return i == last ? s : s.substring(0, i + 1);
            }
        }

        return "";
    }

    public static boolean contentStartsWith(BufferedReader reader, Iterator<String> itr, Pattern ignored) throws IOException {
        String line;
        while (itr.hasNext() && (line = reader.readLine()) != null) {
            if (ignored != null && ignored.matcher(line).find()) {
                continue;
            }

            if (!line.equals(itr.next())) {
                return false;
            }
        }

        return !itr.hasNext();
    }

    public static String skipEmptyLines(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!stripIndent(line).isEmpty()) {
                return line;
            }
        }

        return null;
    }

}
