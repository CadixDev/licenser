/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015, Minecrell <https://github.com/Minecrell>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.cadixdev.gradle.licenser.util;

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
        if (s.isEmpty()) {
            return s;
        }

        final int len = s.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return i == 0 ? s : s.substring(i);
            }
        }

        return "";
    }

    public static String stripTrailingIndent(String s) {
        if (s.isEmpty()) {
            return s;
        }

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

    public static boolean isBlank(String s) {
        return stripIndent(s).isEmpty();
    }

    public static String skipEmptyLines(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!isBlank(line)) {
                return line;
            }
        }

        return null;
    }

}
