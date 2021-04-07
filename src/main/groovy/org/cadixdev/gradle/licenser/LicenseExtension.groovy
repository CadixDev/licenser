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

import groovy.transform.PackageScope
import org.cadixdev.gradle.licenser.header.HeaderFormatRegistry
import org.gradle.api.Action
import org.gradle.api.Incubating
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.resources.TextResourceFactory
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 * Represents the Gradle extension for configuring the settings for the
 * {@link Licenser} plugin.
 */
class LicenseExtension extends LicenseProperties {

    /**
     * The charset to read/write the files with.
     * By default this is {@code UTF-8}.
     */
    Property<String> getCharset() {
        return super.charset // groovy has no distinction between methods and fields...
    }

    /**
     * Whether to ignore failures and only warn about license violations
     * instead of failing the build.
     * By default this is {@code false}.
     */
    final Property<Boolean> ignoreFailures

    /**
     * Whether to skip existing license headers instead of failing the build or
     * updating the license headers.
     * By default this is {@code false}.
     */
    final Property<Boolean> skipExistingHeaders

    /**
     * The line ending to use within license headers.
     *
     * By default this is {@link System#lineSeparator()}
     */
    final Property<String> lineEnding

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
    final ListProperty<String> keywords

    /**
     * Additional conditional {@link LicenseProperties} matching a subset of
     * the files in the project's source sets.
     */
    final ListProperty<LicenseProperties> conditionalProperties

    /**
     * Additional custom license tasks that operate on a listed set of files
     * (not necessarily source sets). Can be used to apply license headers to
     * sources outside of source sets.
     */
    @Incubating
    final NamedDomainObjectContainer<LicenseTaskProperties> tasks

    @PackageScope final ObjectFactory objects
    @PackageScope final TextResourceFactory textResources
    @PackageScope final ProviderFactory providers

    @Inject
    LicenseExtension(final ObjectFactory objects, final Project project) {
        super(new PatternSet(), objects, project.resources.text)

        this.objects = objects
        this.textResources = project.resources.text
        this.providers = project.providers
        this.tasks = objects.domainObjectContainer(LicenseTaskProperties) { String name ->
            new LicenseTaskProperties((filter as PatternSet).intersect(), name, objects, textResources, charset)
        }
        this.conditionalProperties = objects.listProperty(LicenseProperties)

        // Defaults
        this.keywords = objects.listProperty(String).convention(['Copyright', 'License'])
        this.ignoreFailures = objects.property(Boolean).convention(false)
        this.skipExistingHeaders = objects.property(Boolean).convention(false)
        this.charset.convention('UTF-8')

        def defaultLineEnding
        try {
            // Gradle 6+
            defaultLineEnding = project.providers.systemProperty("line.separator")
        } catch (final MissingMethodException ex) {
            // Gradle 5.x TODO @ 0.7: remove this
            defaultLineEnding = project.provider { System.lineSeparator() }
        }
        this.lineEnding = objects.property(String).convention(defaultLineEnding)
        this.newLine.convention(true)

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
     * @see #charset
     */
    void charset(final String charset) {
        this.charset.set(charset)
    }

    /**
     * @see #ignoreFailures
     */
    void ignoreFailures(final boolean ignoreFailures) {
        this.ignoreFailures.set(ignoreFailures)
    }

    /**
     * @see #skipExistingHeaders
     */
    void skipExistingHeaders(final boolean skipExistingHeaders) {
        this.skipExistingHeaders.set(skipExistingHeaders)
    }

    /**
     * @see #lineEnding
     */
    void lineEnding(final String lineEnding) {
        this.lineEnding.set(lineEnding)
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
        conditionalProperties.add(ConfigureUtil.configure(closure, new LicenseProperties(pattern, this.objects, this.textResources, this.charset)))
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

    /**
     * Add additional keywords that indicate a license header.
     *
     * @param keywords the extra keywords
     */
    void keywords(final String... keywords) {
        this.keywords.addAll(keywords);
    }

    /**
     * Configure the extra properties that can be used in the license plugin.
     *
     * <p>This is mostly useful for Kotlin buildscripts which have scope issues
     * for the normal way of working with extra properties.</p>
     *
     * @param action the action to perform
     */
    void properties(final Action<ExtraPropertiesExtension> action) {
        ExtraPropertiesExtension extra = this.ext
        action.execute(extra)
    }

    // kotlin + from other plugins
    /**
     * Set the header to the contents of a file.
     *
     * @param header anything accepted in {@link org.gradle.api.Project#file(Object)}
     * @see #header
     */
    @Override
    void header(final Object header) {
        this.header.set(this.charset.map { this.textResources.fromFile(header, it) })
    }

    // groovy
    @Override
    void setHeader(final File header) {
        this.header.set(this.charset.map { this.textResources.fromFile(header, it) })
    }

}
