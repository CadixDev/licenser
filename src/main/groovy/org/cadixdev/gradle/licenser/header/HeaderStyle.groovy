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

import javax.annotation.Nullable
import java.util.regex.Pattern

enum HeaderStyle {
    BLOCK_COMMENT(~/^\s*\/\*(?:[^*].*)?$/, ~/\*\/\s*(.*?)$/, null, '/*', ' *', ' */',
        'java', 'groovy', 'scala', 'kt', 'kts', 'gradle', 'css', 'js'),
    JAVADOC(~/^\s*\/\*\*(?:[^*].*)?$/, ~/\*\/\s*(.*?)$/, null, '/**', ' *', ' */'),
    HASH(~/^\s*#/, null, ~/^\s*#!/, '#', '#', '#', 'properties', 'yml', 'yaml', 'sh'),
    XML(~/^\s*<!--/, ~/-->\s*(.*?)$/, ~/^\s*<(?:\?xml .*\?|!DOCTYPE .*)>\s*$/, '<!--', '   ', '-->',
            'xml', 'xsd', 'xsl', 'fxml', 'dtd', 'html', 'xhtml')

    final CommentHeaderFormat format
    private final String[] extensions

    HeaderStyle(Pattern start, @Nullable Pattern end, @Nullable Pattern skipLine,
                String firstLine, String prefix, String lastLine, String... extensions) {
        this.format = new CommentHeaderFormat(this.name(), start, end, skipLine, firstLine, prefix, lastLine)
        this.extensions = extensions
    }

    void register(HeaderFormatRegistry registry) {
        extensions.each { registry[it] = this }
    }

}
