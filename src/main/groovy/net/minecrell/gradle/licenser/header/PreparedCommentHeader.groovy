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
    boolean update(File file, String charset, Closure<File> callback) throws IOException {
        boolean valid = false
        String last = null
        String text = null

        file.withReader(charset) { BufferedReader reader ->
            String line = HeaderHelper.skipEmptyLines(reader)
            if (line == null) {
                return
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
                            if (valid) {
                                while (valid && itr.hasNext()) {
                                    line = reader.readLine()
                                    if (line == null) {
                                        valid = false
                                        return
                                    }

                                    if (line != itr.next()) {
                                        valid = false
                                        last = line
                                    }
                                }
                            }
                        }

                        break
                    }
                } else if (!(line =~ format.start)) {
                    // Check if really valid
                    if (valid && itr.hasNext()) {
                        while (true) {
                            if (line != itr.next()) {
                                valid = false
                                break
                            }

                            if (!itr.hasNext()) {
                                break
                            }

                            line = reader.readLine()
                            if (line == null) {
                                valid = false
                                return
                            }
                        }
                    }

                    if (!valid) {
                        last = line
                    }
                }
            }

            text = reader.text
            return
        }

        if (valid) {
            return false // Nothing to do
        }

        file = callback.call(file)

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
