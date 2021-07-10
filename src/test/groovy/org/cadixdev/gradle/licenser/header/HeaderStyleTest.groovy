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

import spock.lang.Specification
import spock.lang.Unroll

class HeaderStyleTest extends Specification {
    @Unroll
    def "test header style #style"() {
        when:
        def lines = style.format.format("Test")

        then:
        lines == expectedLines

        where:
        [style, expectedLines] << [
                [HeaderStyle.BLOCK_COMMENT, ["/*", " * Test", " */"]],
                [HeaderStyle.JAVADOC, ["/**", " * Test", " */"]],
                [HeaderStyle.DOUBLE_SLASH, ["//", "// Test", "//"]],
                [HeaderStyle.HASH, ["#", "# Test", "#"]],
                [HeaderStyle.XML, ["<!--", "    Test", "-->"]]
        ]
    }

    def "DOUBLE_SLASH may contain slashes"() {
        given:
        HeaderStyle style = HeaderStyle.DOUBLE_SLASH

        when:
        def lines = style.format.format("// Test //")

        then:
        lines == ["//", "// // Test //", "//"]
    }

    def "BLOCK_COMMENT may not contain end of comment"() {
        given:
        HeaderStyle style = HeaderStyle.BLOCK_COMMENT

        when:
        style.format.format("/* Test */")

        then:
        thrown(IllegalArgumentException)
    }
}
