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

package org.cadixdev.gradle.licenser.tasks

import org.cadixdev.gradle.licenser.header.Header
import org.cadixdev.gradle.licenser.header.PreparedHeader
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileTreeElement
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.util.PatternFilterable

import javax.inject.Inject

abstract class LicenseTask extends DefaultTask {

    @Nested
    abstract ListProperty<Header> getHeaders()

    @Internal
    FileCollection files

    @Input
    PatternFilterable filter

    @Input
    abstract Property<String> getCharset()

    @Input
    abstract Property<Boolean> getSkipExistingHeaders()

    @InputFiles
    @SkipWhenEmpty
    @IgnoreEmptyDirectories
    @PathSensitive(PathSensitivity.ABSOLUTE)
    FileTree getMatchingFiles() {
        def tree = this.files.asFileTree
        return filter != null ? tree.matching(filter) : tree
    }

    void headers(Header... headers) {
        this.headers.addAll(headers)
    }

    void headers(final Action<ListProperty<Header>> headers) {
        headers.execute(this.headers)
    }

    void charset(final String charset) {
        this.charset.set(charset)
    }

    void skipExistingHeaders(final Boolean skip) {
        this.skipExistingHeaders.set(skip)
    }

    @Inject
    protected abstract ProjectLayout getLayout()

    protected PreparedHeader prepareMatchingHeader(FileTreeElement element, File file) {
        def header = getMatchingHeader(element)
        if (header == null) {
            logger.info("No matching header found for {}", getSimplifiedPath(file))
            return null
        }

        if (header.text.empty) {
            return null
        }

        def prepared = header.prepare(file)
        if (prepared == null) {
            logger.info("No matching header format found for {}", getSimplifiedPath(file))
            return null
        }

        return prepared
    }

    protected Header getMatchingHeader(FileTreeElement element) {
        return getHeaders().get().find { it.filter.isSatisfiedBy(element) }
    }

    private String projectPath

    protected String getSimplifiedPath(File file) {
        if (projectPath == null) {
            projectPath = layout.projectDirectory.asFile.canonicalPath
        }

        def path = file.canonicalPath
        return path.startsWith(projectPath) ? path[projectPath.length()+1..-1] : path
    }

}
