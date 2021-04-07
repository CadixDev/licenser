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
import org.gradle.api.file.FileTreeElement
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.specs.Specs
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.util.PatternSet

class Header {

    @Internal
    final HeaderFormatRegistry registry

    @Input
    final Provider<List<String>> keywords

    private final Provider<String> loader

    @Input
    final Spec<FileTreeElement> filter

    @Input
    final Provider<Boolean> newLine

    private String text

    private final Map<HeaderFormat, PreparedHeader> formatted = new IdentityHashMap<>()

    Header(HeaderFormatRegistry registry, ListProperty<String> keywords, Provider<String> loader, PatternSet filter, Provider<Boolean> newLine) {
        this.registry = registry
        this.keywords = keywords.map { it*.toLowerCase() }
        this.loader = loader
        this.filter = filter?.asSpec ?: Specs.satisfyAll()
        this.newLine = newLine
    }

    @Input
    String getText() {
        if (this.text == null) {
            this.text = this.loader.get()
            if (!containsKeyword(this.text)) {
                throw new IllegalArgumentException("Header does not contain any of the required keywords: $keywords")
            }
        }

        return this.text
    }

    boolean containsKeyword(String s) {
        s = s.toLowerCase()
        return keywords.get().any(s.&contains)
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
