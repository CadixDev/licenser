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

import org.cadixdev.gradle.licenser.header.HeaderFormatRegistry
import org.gradle.api.Incubating
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.ConfigureUtil

/**
 * Represents the Gradle extension for configuring the settings for the
 * {@link Licenser} plugin.
 */
class LicenseExtension extends LicenseProperties {

    /**
     * The source sets to scan for files with license headers.
     * By default this includes all source sets of the project.
     */
    Collection<SourceSet> sourceSets

    /**
     * The Android source sets to scan for files with license headers.
     * By default this includes all source sets of the project.
     *
     * <p><b>Note:</b> This is only used when the Android Gradle plugin is applied.</p>
     */
    @Incubating
    Collection androidSourceSets

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
     * Whether to skip existing license headers instead of failing the build or
     * updating the license headers.
     * By default this is {@code false}.
     */
    boolean skipExistingHeaders = false

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

    /**
     * Additional conditional {@link LicenseProperties} matching a subset of
     * the files in the specified {@link #sourceSets}.
     */
    final List<LicenseProperties> conditionalProperties = []

    /**
     * Additional custom license tasks that operate on a listed set of files
     * (not necessarily source sets). Can be used to apply license headers to
     * sources outside of source sets.
     */
    @Incubating
    final NamedDomainObjectContainer<LicenseTaskProperties> tasks

    LicenseExtension(Project project) {
        super(new PatternSet())

        this.tasks = project.container(LicenseTaskProperties) { name ->
            new LicenseTaskProperties((filter as PatternSet).intersect(), name)
        }

        // Defaults
        newLine = true
        charset = 'UTF-8'

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

        // Binary files
        exclude '**/*.zip'
        exclude '**/*.jar'
        exclude '**/*.class'
        exclude '**/*.bin'

        // Manifest
        exclude '**/MANIFEST.MF'
        exclude '**/META-INF/services/**'
    }

    /**
     * Adds a new conditional license header that will be applied to all matching files.
     *
     * @param include A single include pattern
     * @param closure The closure that configures the license header
     */
    void matching(String include, @DelegatesTo(LicenseProperties) Closure closure) {
        matching(new PatternSet().include(include), closure)
    }

    /**
     * Adds a new conditional license header that will be applied to all matching files.
     *
     * @param args A map definition of the pattern, similar to {@link Project#fileTree(Map)}
     * @param closure The closure that configures the license header
     */
    void matching(Map<String, ?> args, @DelegatesTo(LicenseProperties) Closure closure) {
        matching(ConfigureUtil.configureByMap(args, new PatternSet()), closure)
    }

    /**
     * Adds a new conditional license header that will be applied to all matching files.
     *
     * @param patternClosure A closure that configures the {@link PatternFilterable}
     * @param configureClosure The closure that configures the license header
     */
    void matching(@DelegatesTo(PatternFilterable) Closure patternClosure, @DelegatesTo(LicenseProperties) Closure configureClosure) {
        matching(ConfigureUtil.configure(patternClosure, new PatternSet()), configureClosure)
    }

    /**
     * Adds a new conditional license header that will be applied to all matching files.
     *
     * @param pattern The pattern that matches the files
     * @param closure The closure that configures the license header
     */
    void matching(PatternSet pattern, @DelegatesTo(LicenseProperties) Closure closure) {
        conditionalProperties.add(ConfigureUtil.configure(closure, new LicenseProperties(pattern)))
    }

    /**
     * Configures the license styles using the specified {@link Closure}.
     *
     * @param closure The closure to apply to the style
     */
    void style(@DelegatesTo(HeaderFormatRegistry) Closure closure) {
        style.with(closure)
    }

    /**
     * Configures the custom tasks using the specified {@link Closure}.
     *
     * @param closure The closure to apply to the custom tasks
     */
    @Incubating
    void tasks(Closure closure) {
        this.tasks.configure(closure)
    }

}
