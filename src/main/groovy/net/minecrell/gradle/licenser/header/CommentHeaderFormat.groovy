package net.minecrell.gradle.licenser.header

import net.minecrell.gradle.licenser.util.StringHelper

class CommentHeaderFormat implements HeaderFormat {

    final String name
    final String start
    final String prefix
    final String end
    CommentHeaderFormat(String name, String start, String prefix, String end) {
        this.name = name
        this.start = start
        this.prefix = prefix
        this.end = end
    }

    protected List<String> format(String text, boolean newLine) {
        ensureAbsent(text, start)
        ensureAbsent(text, end)

        List<String> result = [start]

        text.eachLine {
            result << StringHelper.trimEnd("$prefix $it")
        }

        result << end

        if (newLine) {
            result << ''
        }
        return result
    }

    private static void ensureAbsent(String s, String search) {
        if (s.contains(search)) {
            throw new IllegalArgumentException("Header contains unsupported characters $search")
        }
    }

    @Override
    PreparedHeader prepare(String text, boolean newLine) {
        return new PreparedCommentHeader(this, format(text, newLine))
    }

}
