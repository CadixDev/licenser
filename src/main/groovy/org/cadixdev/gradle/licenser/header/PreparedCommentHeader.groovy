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

package org.cadixdev.gradle.licenser.header

import org.cadixdev.gradle.licenser.util.HeaderHelper

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
    boolean check(File file, String charset, boolean skipExistingHeaders) throws IOException {
        return file.withReader(charset) { BufferedReader reader ->
            boolean result = skipExistingHeaders ?
                    HeaderHelper.contentStartsWithValidHeaderFormat(reader, format) :
                    HeaderHelper.contentStartsWith(reader, this.lines.iterator(), format.skipLine)
            if (result) {
                def line = reader.readLine()
                if (header.newLine) {
                    result = line != null && line.isEmpty()
                } else if (line != null) {
                    result = !line.isEmpty()
                }
            }
            return result
        }
    }

    @Override
    boolean update(File file, String charset, boolean skipExistingHeaders, Runnable callback) throws IOException {
        boolean valid = false
        // The lines skipped before the license header
        List<String> before = []
        // The comment lines we've read. Important if the comment does not contain
        // any of the keywords, then we need to write it back to the file.
        List<String> comment = null
        // The last line we've looked at
        String last = null
        // The remaining text to write back to the file
        String text = null

        // Open file for verifying the license header and reading the text we
        // need to append after it
        file.withReader(charset) { BufferedReader reader ->
            if (skipExistingHeaders) {
                def startsWithValidHeader = HeaderHelper.contentStartsWithValidHeaderFormat(reader, format)
                if (startsWithValidHeader) {
                    valid = true
                    return
                } else {
                    reader.reset()
                }
            }

            String line
            while (true) {
                // Find first non-empty line
                line = HeaderHelper.skipEmptyLines(reader)
                if (line == null) {
                    return // EOF, invalid and done
                }

                // Unless the line is requested to be skipped by the header we're done
                // However, some header formats have certain lines that need to be skipped
                // E.g. for XML the XML document declaration
                if (!format.skipLine || !(line =~ format.skipLine)) {
                    break
                }

                // Append the lines we've skipped so we can add them back to the file later
                before << line
            }

            // If the first line doesn't match the comment start we're done
            // and the file doesn't have a license header yet
            if (!(line =~ format.start) || (format.end && line =~ format.end)) {
                last = line
                text = reader.text
                return
            }

            // If the first line does not contain one of our license header
            // keywords, we need to start collecting all comment lines we've read
            if (!header.containsKeyword(line)) {
                comment = [line]
            }

            // Now go through the license header and verify it
            valid = true
            def itr = this.lines.iterator()
            while (true) {
                // No lines left in our expected license header; this comment is invalid
                if (!itr.hasNext()) {
                    valid = false
                }

                // Still valid, but next lines doesn't match; invalid comment
                if (valid && itr.next() != line) {
                    valid = false
                }

                // Read the next line from the file
                line = reader.readLine()
                if (line == null) {
                    // EOF, but the end comment was yet found
                    if (format.end) {
                        // Failed to find end of comment. This often means the end
                        // of comment was simply removed, so instead of wiping the
                        // whole file simply throw an exception
                        throw new IllegalStateException("Failed to find end of block comment in $file")
                    }

                    // Nothing needed to end a comment: Invalid but fine to continue
                    valid = false
                    return
                }

                // Only some comment formats have specific patterns for the end of a comment
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

                        // Check for remaining stuff on the comment line
                        // (We don't want to wipe lines from the file)
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

                        // Check if really, really valid
                        if (valid) {
                            valid = line == itr.next()
                            // There are still lines left that would need to come, invalid header
                            if (itr.hasNext()) {
                                valid = false
                            }
                        }

                        // Read one more line so we can check for new lines
                        last = reader.readLine()
                        break
                    }
                } else if (!(line =~ format.start)) {
                    // There are still lines left that would need to come, invalid header
                    if (itr.hasNext()) {
                        valid = false
                    }

                    // The next line is actually the current one, because it is no longer
                    // part of the comment
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

            // Look more carefully at the new lines
            if (valid) {
                if (header.newLine) {
                    // Only valid if next line is empty
                    valid = last != null && last.isEmpty()
                } else if (last != null) {
                    // Only valid if next line is NOT empty
                    valid = !HeaderHelper.isBlank(last)
                }
            }

            if (last != null && HeaderHelper.isBlank(last)) {
                // Skip empty lines
                while ((last = reader.readLine()) != null && HeaderHelper.isBlank(last)) {
                    // Duplicate new lines, NEVER valid
                    valid = false
                }
            }

            if (last != null) {
                // Read the remaining text from the file so we can add it back later
                text = reader.text
            }
            return
        }

        // License header is valid, nothing to do
        if (valid) {
            return false
        }

        // Run callback (used for creating a backup of the files)
        callback.run()

        // Open file for updating license header
        file.withWriter(charset) { BufferedWriter writer ->

            // Write lines that were skipped before the header
            before.each writer.&writeLine

            // Write actual license header
            this.lines.each writer.&writeLine

            // Add new line if requested
            if (header.newLine) {
                writer.newLine()
            }

            // Add comment that was collected but did not match a valid license header
            // (Did not contain any of the defined keywords)
            if (comment != null) {
                comment.each writer.&writeLine
            }

            // Write the last line we have looked at additionally
            // (Only if we have a captured (non-license header) comment before this
            // or it is not empty, we handle the new lines for license header ourselves)
            if (last != null && (comment != null || !last.isEmpty())) {
                writer.writeLine(last)
            }

            // Write the remaining file
            if (text != null) {
                writer.write(text)
            }
        }

        return true
    }

}
