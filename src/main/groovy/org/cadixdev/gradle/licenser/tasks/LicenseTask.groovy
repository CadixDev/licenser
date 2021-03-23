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
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileTreeElement
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.util.PatternFilterable

class LicenseTask extends DefaultTask {

    @Input
    List<Header> headers

    @Internal
    FileCollection files

    @Input
    PatternFilterable filter

    @Input
    String charset

    @Input
    boolean skipExistingHeaders

    @InputFiles
    @SkipWhenEmpty
    FileTree getMatchingFiles() {
        def tree = this.files.asFileTree
        return filter != null ? tree.matching(filter) : tree
    }

    protected PreparedHeader prepareMatchingHeader(FileTreeElement element, File file) {
        def header = getMatchingHeader(element)
        if (header == null) {
            logger.warn("No matching header found for {}", getSimplifiedPath(file))
            return null
        }

        if (header.text.empty) {
            return null
        }

        def prepared = header.prepare(file)
        if (prepared == null) {
            logger.warn("No matching header format found for {}", getSimplifiedPath(file))
            return null
        }

        return prepared
    }

    protected Header getMatchingHeader(FileTreeElement element) {
        return headers.find { it.filter.isSatisfiedBy(element) }
    }

    private String projectPath

    protected String getSimplifiedPath(File file) {
        if (projectPath == null) {
            projectPath = project.projectDir.canonicalPath
        }

        def path = file.canonicalPath
        return path.startsWith(projectPath) ? path[projectPath.length()+1..-1] : path
    }

}
