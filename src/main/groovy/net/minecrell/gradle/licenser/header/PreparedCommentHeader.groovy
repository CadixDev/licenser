/*
 * Licenser
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

package net.minecrell.gradle.licenser.header

import net.minecrell.gradle.licenser.util.HeaderHelper

class PreparedCommentHeader implements PreparedHeader {

    final Header header
    final CommentHeaderFormat format
    final List<String> lines

    PreparedCommentHeader(Header header, CommentHeaderFormat format, List<String> lines) {
        this.header = header
        this.format = format
        this.lines = lines
    }

    @Override
    boolean check(File file, String charset) throws IOException {
        return file.withReader(charset) { BufferedReader reader ->
            boolean result = HeaderHelper.contentStartsWith(reader, this.lines.iterator(), format.skipLine)
            if (result && header.newLine) {
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
        List<String> comment = null
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

            if (!header.containsKeyword(line)) {
                comment = [line]
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
                    if (format.end) {
                        throw new IllegalStateException("Failed to find end of block comment in $file")
                    }
                    valid = false
                    return
                }

                if (format.end) {
                    // Multi-line
                    def matcher = line =~ format.end
                    if (matcher) {
                        // Append comment to buffer if it doesn't contain keyword
                        if (comment != null) {
                            if (header.containsKeyword(line)) {
                                comment = null
                            } else {
                                comment << line
                            }
                        }

                        if (matcher.hasGroup()) {
                            def group = matcher.group(1)
                            if (!group.isEmpty()) {
                                // There is stuff after the comment has ended, never valid
                                valid = false
                                if (!comment) {
                                    last = group
                                }
                                break
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

                // Append comment to buffer if it doesn't contain keyword
                if (comment != null) {
                    if (header.containsKeyword(line)) {
                        comment = null
                    } else {
                        comment << line
                    }
                }
            }

            if (valid && header.newLine) {
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
            if (header.newLine) {
                writer.newLine()
            }
            if (comment != null) {
                comment.each writer.&writeLine
            }
            if (last != null && (comment != null || !(last.isEmpty() && header.newLine))) {
                writer.writeLine(last)
            }
            if (text != null) {
                writer.write(text)
            }
        }

        return true
    }

}
