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

package org.cadixdev.gradle.licenser

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.resources.TextResource
import org.gradle.api.resources.TextResourceFactory
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet

import javax.inject.Inject

/**
 * Specifies how to format the license header of a subset of the files, defined
 * by the {@link #filter}.
 */
class LicenseProperties implements PatternFilterable {

    /**
     * The filter to apply to the source files.
     * By default this only includes a few excludes for binary files or files
     * without standardized comment formats.
     */
    @Delegate
    PatternFilterable filter

    /**
     * The path to the file containing the license header.
     * By default this is the {@code LICENSE} file in the project directory.
     */
    final Property<TextResource> header

    /**
     * Whether to insert an empty line after the license header.
     * By default this is {@code true}.
     */
    final Property<Boolean> newLine

    /**
     * Pay no attention to the line after the license header.
     * By default this is {@code false}.
     */
    final Property<Boolean> ignoreNewLine

    protected final Property<String> charset
    private final TextResourceFactory resources

    @Inject
    LicenseProperties(PatternSet filter, ObjectFactory objects, TextResourceFactory resources) {
        this.filter = filter
        this.charset = objects.property(String)
        this.header = objects.property(TextResource)
        this.newLine = objects.property(Boolean)
        this.ignoreNewLine = objects.property(Boolean)
        this.resources = resources
    }

    @Inject
    LicenseProperties(PatternSet filter, ObjectFactory objects, TextResourceFactory resources, Property<String> charset) {
        this(filter, objects, resources)
        this.charset.set(charset)
    }

    /**
     * @see #header
     * @param header the new header
     */
    void header(final TextResource header) {
        this.header.set(header)
    }

    // kotlin + from other plugins
    /**
     * Set the header to the contents of a file.
     *
     * @param header anything accepted in {@link org.gradle.api.Project#file(Object)}
     * @see #header
     */
    void header(final Object header) {
        this.header.set(this.charset.map { this.resources.fromFile(header, it) })
    }

    // groovy
    void setHeader(final File header) {
        this.header.set(this.charset.map { this.resources.fromFile(header, it) })
    }

    void newLine(final Boolean newLine) {
        this.newLine.set(newLine)
    }

    void ignoreNewLine(final Boolean ignoreNewLine) {
        this.ignoreNewLine.set(ignoreNewLine)
    }

}
