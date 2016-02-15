package net.minecrell.gradle.licenser.header

import net.minecrell.gradle.licenser.util.HeaderHelper

class PreparedCommentHeader implements PreparedHeader {

    final CommentHeaderFormat format
    final List<String> lines
    final boolean newLine

    PreparedCommentHeader(CommentHeaderFormat format, List<String> lines, boolean newLine) {
        this.format = format
        this.lines = lines
        this.newLine = newLine
    }

    @Override
    boolean check(File file, String charset) throws IOException {
        return file.withReader(charset) { BufferedReader reader ->
            boolean result = HeaderHelper.contentStartsWith(reader, this.lines.iterator(), format.skipLine)
            if (result && newLine) {
                def line = reader.readLine()
                result = line != null && line.isEmpty()
            }
            return result
        }
    }

    @Override
    boolean update(File file, String charset, Runnable callback) throws IOException {
        boolean valid = false
        List<String> before = []
        String last = null
        String text = null

        file.withReader(charset) { BufferedReader reader ->
            String line
            while (true) {
                line = HeaderHelper.skipEmptyLines(reader)
                if (line == null) {
                    return
                }

                if (!format.skipLine || !(line =~ format.skipLine)) {
                    break
                }

                before << line
            }

            if (!(line =~ format.start)) {
                last = line
                text = reader.text
                return
            }

            valid = true
            def itr = this.lines.iterator()
            while (true) {
                if (!itr.hasNext()) {
                    valid = false
                }

                if (valid && itr.next() != line) {
                    valid = false
                }

                line = reader.readLine()
                if (line == null) {
                    valid = false
                    return
                }

                if (format.end) {
                    // Multi-line
                    def matcher = line =~ format.end
                    if (matcher) {
                        if (matcher.hasGroup()) {
                            def group = matcher.group(1)
                            if (!group.isEmpty()) {
                                // There is stuff after the comment has ended, never valid
                                valid = false
                                last = group
                                text = reader.text
                                return
                            }
                        }

                        // Check if really valid
                        if (valid) {
                            valid = line == itr.next()
                            if (itr.hasNext()) {
                                valid = false
                            }
                        }

                        last = reader.readLine()
                        break
                    }
                } else if (!(line =~ format.start)) {
                    // If there is something left it is invalid
                    if (itr.hasNext()) {
                        valid = false
                    }

                    last = line
                    break
                }
            }

            if (valid && newLine) {
                // Only valid if there is a new line
                valid = last != null && last.isEmpty()
            }

            text = reader.text
            return
        }

        if (valid) {
            return false // Nothing to do
        }

        callback.run()

        file.withWriter { BufferedWriter writer ->
            before.each writer.&writeLine
            this.lines.each writer.&writeLine
            if (newLine) {
                writer.newLine()
            }
            if (last != null && !(last.isEmpty() && newLine)) {
                writer.writeLine(last)
            }
            if (text != null) {
                writer.write(text)
            }
        }

        return true
    }

}
