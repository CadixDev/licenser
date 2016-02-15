package net.minecrell.gradle.licenser.header

import net.minecrell.gradle.licenser.util.HeaderHelper

import java.util.regex.Pattern

class CommentHeaderFormat implements HeaderFormat {

    final String name

    final Pattern start
    final Pattern end
    final Pattern skipLine

    final String firstLine
    final String prefix
    final String lastLine

    CommentHeaderFormat(String name, Pattern start, Pattern end, Pattern skipLine, String firstLine, String prefix, String lastLine) {
        this.name = name
        this.start = start
        this.end = end
        this.skipLine = skipLine
        this.firstLine = firstLine
        this.prefix = prefix
        this.lastLine = lastLine
    }

    protected List<String> format(String text) {
        ensureAbsent(text, firstLine)
        ensureAbsent(text, lastLine)

        List<String> result = [firstLine]

        text.eachLine {
            result << HeaderHelper.stripTrailingIndent("$prefix $it")
        }

        result << lastLine
        return result
    }

    private static void ensureAbsent(String s, String search) {
        if (s.contains(search)) {
            throw new IllegalArgumentException("Header contains unsupported characters $search")
        }
    }

    @Override
    PreparedHeader prepare(String text, boolean newLine) {
        return new PreparedCommentHeader(this, format(text), newLine)
    }

    @Override
    String toString() {
        return name
    }

}
