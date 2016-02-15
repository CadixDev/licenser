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

package net.minecrell.gradle.licenser

import net.minecrell.gradle.licenser.header.HeaderFormatRegistry
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet

/**
 * Represents the Gradle extension for configuring the settings for the
 * {@link Licenser} plugin.
 */
class LicenseExtension implements PatternFilterable {

    /**
     * The filter to apply to the source files of the {@link #sourceSets}.
     * By default this only includes a few excludes for binary files or files
     * without standardized comment formats.
     */
    @Delegate
    PatternFilterable filter = new PatternSet()

    /**
     * The path to the file containing the license header.
     * By default this is the {@code LICENSE} file in the project directory.
     */
    File header

    /**
     * Whether to insert an empty line after the license header.
     * By default this is {@code true}.
     */
    boolean newLine = true

    /**
     * The source sets to scan for files with license headers.
     * By default this includes all source sets of the project.
     */
    Collection<SourceSet> sourceSets

    /**
     * The charset to read/write the files with.
     * By default this is {@code UTF-8}.
     */
    String charset = 'UTF-8'

    /**
     * Whether to ignore failures and only warn about license violations
     * instead of failing the build.
     * By default this is {@code false}.
     */
    boolean ignoreFailures = false

    /**
     * The style mappings from file extension to the type of style of the
     * comment header for the license header.
     * By default this includes mappings and styles for the most common file
     * types.
     */
    HeaderFormatRegistry style = new HeaderFormatRegistry()

    /**
     * The (case-insensitive) keywords that identify a comment as license
     * header.
     * By default this includes only the words "Copyright" and "License".
     */
    List<String> keywords = ['Copyright', 'License']

    LicenseExtension() {
        // Files without standard comment format
        exclude '**/*.txt'
        exclude '**/*.json'
        exclude '**/*.md'

        // Image files
        exclude '**/*.jpg'
        exclude '**/*.png'
        exclude '**/*.gif'
        exclude '**/*.bmp'
        exclude '**/*.ico'

        // Manifest
        exclude '**/MANIFEST.MF'
        exclude '**/META-INF/services/**'
    }

    /**
     * Configures the license styles using the specified closure.
     *
     * @param closure The closure to apply to the style
     */
    void style(Closure closure) {
        closure.delegate = style
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call(style)
    }

}
