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

package org.cadixdev.gradle.licenser.util


import org.cadixdev.gradle.licenser.header.HeaderStyle
import spock.lang.Specification

class HeaderHelperTest extends Specification {
    def "contentStartsWithValidHeaderFormat returns false with empty input"() {
        given:
        def inputString = ""
        def stringReader = new StringReader(inputString)
        def reader = new BufferedReader(stringReader)

        when:
        def result = HeaderHelper.contentStartsWithValidHeaderFormat(reader, HeaderStyle.BLOCK_COMMENT.format)

        then:
        !result
    }

    def "contentStartsWithValidHeaderFormat returns false with non-matching input"() {
        given:
        def inputString = "Not a copyright header"
        def stringReader = new StringReader(inputString)
        def reader = new BufferedReader(stringReader)

        when:
        def result = HeaderHelper.contentStartsWithValidHeaderFormat(reader, HeaderStyle.BLOCK_COMMENT.format)

        then:
        !result
    }

    def "contentStartsWithValidHeaderFormat returns true with valid non-empty header"() {
        given:
        def inputString = """\
            /*
             * Some copyright header
             */
            My Content
        """.stripIndent()
        def stringReader = new StringReader(inputString)
        def reader = new BufferedReader(stringReader)

        when:
        def result = HeaderHelper.contentStartsWithValidHeaderFormat(reader, HeaderStyle.BLOCK_COMMENT.format)

        then:
        result
    }

    def "contentStartsWithValidHeaderFormat returns false with invalid non-empty header"() {
        given:
        def inputString = """\
            /**
             * Some copyright header
             */
            My Content
        """.stripIndent()
        def stringReader = new StringReader(inputString)
        def reader = new BufferedReader(stringReader)

        when:
        def result = HeaderHelper.contentStartsWithValidHeaderFormat(reader, HeaderStyle.BLOCK_COMMENT.format)

        then:
        !result
    }

    def "contentStartsWithValidHeaderFormat returns true with valid empty header"() {
        given:
        def inputString = """\
            /*
             */
            My Content
        """.stripIndent()
        def stringReader = new StringReader(inputString)
        def reader = new BufferedReader(stringReader)

        when:
        def result = HeaderHelper.contentStartsWithValidHeaderFormat(reader, HeaderStyle.BLOCK_COMMENT.format)

        then:
        result
    }

    def "contentStartsWithValidHeaderFormat returns false with incomplete header"() {
        given:
        def inputString = """\
            /*
             * Incomplete copyright header
            My Content
        """.stripIndent()
        def stringReader = new StringReader(inputString)
        def reader = new BufferedReader(stringReader)

        when:
        def result = HeaderHelper.contentStartsWithValidHeaderFormat(reader, HeaderStyle.BLOCK_COMMENT.format)

        then:
        !result
    }

    def "contentStartsWithValidHeaderFormat returns true with valid header with ignored lines"() {
        given:
        def inputString = """\
            #!/bin/bash
            # Some header
            My Content
        """.stripIndent()
        def stringReader = new StringReader(inputString)
        def reader = new BufferedReader(stringReader)

        when:
        def result = HeaderHelper.contentStartsWithValidHeaderFormat(reader, HeaderStyle.HASH.format)

        then:
        result
    }

    def "contentStartsWithValidHeaderFormat returns true with valid header with XML"() {
        given:
        def inputString = """\
            <?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
            <!--
               Some copyright header
            -->
            <document>
            </document>
        """.stripIndent()
        def stringReader = new StringReader(inputString)
        def reader = new BufferedReader(stringReader)

        when:
        def result = HeaderHelper.contentStartsWithValidHeaderFormat(reader, HeaderStyle.XML.format)

        then:
        result
    }
}
