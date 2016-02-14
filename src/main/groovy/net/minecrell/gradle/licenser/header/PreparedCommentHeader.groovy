package net.minecrell.gradle.licenser.header

import net.minecrell.gradle.licenser.util.HeaderHelper

class PreparedCommentHeader implements PreparedHeader {

    final CommentHeaderFormat format
    final List<String> lines

    PreparedCommentHeader(CommentHeaderFormat format, List<String> lines) {
        this.format = format
        this.lines = lines
    }

    @Override
    boolean check(File file, String charset) throws IOException {
        boolean result = false
        file.withReader(charset) { BufferedReader reader ->
            result = HeaderHelper.contentStartsWith(reader, this.lines.iterator())
        }
        return result
    }

    @Override
    boolean update(File file, String charset) throws IOException {
        boolean valid = false
        String last = null
        String text = null

        file.withReader(charset) { BufferedReader reader ->
            String line = HeaderHelper.skipEmptyLines(reader)
            if (line == null) {
                return
            }

            if (!HeaderHelper.stripIndent(line).startsWith(format.start)) {
                last = line
                text = reader.text
                return
            }

            valid = true
            def itr = this.lines.iterator()
            while (true) {
                if (valid && itr.next() != line) {
                    valid = false
                }

                line = reader.readLine()
                if (line == null) {
                    valid = false
                    return
                }

                int pos = line.indexOf(format.end)
                if (pos >= 0) {
                    pos += format.end.length()
                    def trimmed = HeaderHelper.stripTrailingIndent(line)
                    // There is stuff after the comment has ended, never valid
                    if (pos < trimmed.length()) {
                        valid = false
                        last = HeaderHelper.stripIndent(line[pos..-1])
                        text = reader.text
                        return
                    }

                    // Check if really valid
                    if (valid) {
                        valid = line == itr.next()
                        assert !itr.hasNext(), "Cannot have lines after end of header"
                        if (valid) {
                            return
                        }
                    }

                    text = reader.text
                    return
                }
            }
        }

        if (valid) {
            return false // Nothing to do
        }

        file.withWriter { BufferedWriter writer ->
            this.lines.each { writer.writeLine(it) }
            if (last != null) {
                writer.writeLine(last)
            }
            if (text != null) {
                writer.write(text)
            }
        }

        return true
    }

}
