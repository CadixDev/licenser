/*
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
import org.gradle.api.file.FileTreeElement
import org.gradle.api.specs.Spec
import org.gradle.api.specs.Specs
import org.gradle.api.tasks.util.PatternSet

class Header {

    final HeaderFormatRegistry registry
    final List<String> keywords

    private final Closure<String> loader
    final Spec<FileTreeElement> filter

    final boolean newLine
    private String text

    private final Map<HeaderFormat, PreparedHeader> formatted = new IdentityHashMap<>()

    Header(HeaderFormatRegistry registry, List<String> keywords, Closure<String> loader, PatternSet filter, boolean newLine) {
        this.registry = registry
        this.keywords = keywords*.toLowerCase().asImmutable()
        this.loader = loader
        this.filter = filter?.asSpec ?: Specs.satisfyAll()
        this.newLine = newLine
    }

    String getText() {
        if (this.text == null) {
            this.text = loader.call()
            if (!containsKeyword(this.text)) {
                throw new IllegalArgumentException("Header does not contain any of the required keywords: $keywords")
            }
        }

        return this.text
    }

    boolean containsKeyword(String s) {
        s = s.toLowerCase()
        return keywords.any(s.&contains)
    }

    PreparedHeader prepare(HeaderFormat format) {
        if (format == null) {
            return null
        }

        PreparedHeader result = formatted[format]
        if (result == null) {
            result = format.prepare(this, getText())
            formatted[format] = result
        }
        return result
    }

    PreparedHeader prepare(String path) {
        return prepare(registry[HeaderHelper.getExtension(path)])
    }

    PreparedHeader prepare(File file) {
        return prepare(file.path)
    }

}
