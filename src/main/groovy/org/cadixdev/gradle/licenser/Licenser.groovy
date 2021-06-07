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

import groovy.text.SimpleTemplateEngine
import org.cadixdev.gradle.licenser.header.Header
import org.cadixdev.gradle.licenser.tasks.LicenseCheck
import org.cadixdev.gradle.licenser.tasks.LicenseTask
import org.cadixdev.gradle.licenser.tasks.LicenseUpdate
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.provider.ListProperty
import org.gradle.api.resources.ResourceException
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.util.PatternSet
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.util.ConfigureUtil

class Licenser implements Plugin<Project> {

    private static final String CHECK_TASK = 'checkLicense'
    private static final String FORMAT_TASK = 'updateLicense'
    private static final String ANDROID_TASK = 'Android'
    private static final String CUSTOM_TASK = 'Custom'
    private static final String FORMATTING_GROUP = 'formatting'

    private Project project
    private LicenseExtension extension

    @Override
    void apply(Project project) {
        this.project = project
        this.extension = project.extensions.create('license', LicenseExtension, project.objects, project)
        this.extension.header.set(extension.charset.map { charset -> project.resources.text.fromFile('LICENSE', charset) })

        def headers = project.objects.listProperty(Header)
        // TODO: finalizeValueOnRead is not supported on Gradle 5, remove when dropping support
        try {
            extension.conditionalProperties.finalizeValueOnRead()
            headers.finalizeValueOnRead()
        } catch (final MissingMethodException ignored) {
            // no method
        }
        headers.set(extension.conditionalProperties.map {
            def built = []
            it.reverseEach { props ->
                built << prepareHeader(extension, props)
            }
            built << prepareHeader(extension, extension)
            return built
        })

        def plugins = project.plugins
        def tasks = project.tasks

        // Configure tasks from different sources
        plugins.withType(JavaBasePlugin) {
            project.sourceSets.all { SourceSet set ->
                def extensionIgnoreFailures = extension.ignoreFailures
                def extensionLineEnding = extension.lineEnding
                createTask(CHECK_TASK, LicenseCheck, headers, set) {
                    ignoreFailures.set(extensionIgnoreFailures)
                }
                createTask(FORMAT_TASK, LicenseUpdate, headers, set) {
                    lineEnding.set(extensionLineEnding)
                }
            }
        }

        ['com.android.library', 'com.android.application'].each {
            plugins.withId(it) {
                project.android.sourceSets.all { set ->
                    def extensionIgnoreFailures = extension.ignoreFailures
                    def extensionLineEnding = extension.lineEnding
                    createAndroidTask(CHECK_TASK, LicenseCheck, headers, set) {
                        ignoreFailures.set(extensionIgnoreFailures)
                    }
                    createAndroidTask(FORMAT_TASK, LicenseUpdate, headers, set) {
                        lineEnding.set(extensionLineEnding)
                    }
                }
            }
        }

        extension.tasks.all { LicenseTaskProperties props ->
            def extensionIgnoreFailures = extension.ignoreFailures
            def extensionLineEnding = extension.lineEnding
            createCustomTask(CHECK_TASK, LicenseCheck, props) {
                ignoreFailures.set(extensionIgnoreFailures)
            }
            createCustomTask(FORMAT_TASK, LicenseUpdate, props) {
                lineEnding.set(extensionLineEnding)
            }
        }

        // Then configure catch-all tasks
        def globalCheck = tasks.register(CHECK_TASK + 's') {
            dependsOn(tasks.withType(LicenseCheck))
        }
        tasks.register('licenseCheck') {
            dependsOn(globalCheck)
        }
        def globalFormat = tasks.register(FORMAT_TASK + 's') {
            dependsOn(tasks.withType(LicenseUpdate))
        }
        tasks.register('licenseFormat') {
            group = FORMATTING_GROUP
            dependsOn(globalFormat)
        }

        plugins.withType(LifecycleBasePlugin) {
            tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure {
                dependsOn(globalCheck)
            }
        }
    }

    private static Header prepareHeader(LicenseExtension extension, LicenseProperties properties) {
        def headerResource = properties.header.orElse(extension.header)
        def extraProperties = extension.ext
        extension.keywords.disallowChanges()
        properties.newLine.disallowChanges()
        return new Header(extension.style, extension.keywords, extension.providers.provider {
            TextResource header = headerResource.getOrNull()
            if (header != null) {
                def text
                try {
                    text = header.asString()
                } catch (ResourceException ignored) {
                    return ""
                }

                Map<String, String> props = extraProperties.properties
                if (props) {
                    def engine = new SimpleTemplateEngine()
                    def template = engine.createTemplate(text).make(props)
                    text = template.toString()
                }

                return text
            }

            return ""
        }, (PatternSet) properties.filter, properties.newLine.orElse(extension.newLine),)
    }

    private <T extends LicenseTask> TaskProvider<T> createTask(
            String name,
            @DelegatesTo.Target Class<T> type,
            ListProperty<Header> headers,
            SourceSet sourceSet,
            @DelegatesTo(strategy = Closure.DELEGATE_FIRST) Closure target = null
    ) {
        return makeTask(sourceSet.getTaskName(name, null), type, headers, sourceSet.allSource, target)
    }

    private <T extends LicenseTask> TaskProvider<T> createAndroidTask(
            String name,
            @DelegatesTo.Target Class<T> type,
            ListProperty<Header> headers,
            Object sourceSet,
            @DelegatesTo(strategy = Closure.DELEGATE_FIRST) Closure target = null
    ) {
        return makeTask(name + ANDROID_TASK + sourceSet.name.capitalize(), type, headers,
                project.files(sourceSet.java.sourceFiles, sourceSet.res.sourceFiles, sourceSet.manifest.srcFile), target)
    }

    private <T extends LicenseTask> TaskProvider<T> createCustomTask(
            String name,
            @DelegatesTo.Target Class<T> type,
            LicenseTaskProperties properties,
            @DelegatesTo(strategy = Closure.DELEGATE_FIRST) Closure target = null
    ) {
        def headers = project.objects.listProperty(Header)
        headers.add(prepareHeader(extension, properties))
        def task = makeTask(name + CUSTOM_TASK + properties.name.capitalize(), type, headers, properties.files, target)
        task.configure {
            filter = properties.filter
        }
        return task
    }

    private <T extends LicenseTask> TaskProvider<T> makeTask(
            String name,
            Class<T> type,
            ListProperty<Header> headers,
            FileCollection files,
            Closure target = null
    ) {
        def charset = extension.charset
        def skipExisting = extension.skipExistingHeaders
        return project.tasks.register(name, type) { T task ->
            task.headers.set(headers)
            task.files = files
            task.filter = extension.filter
            task.charset.set(charset)
            task.skipExistingHeaders.set(skipExisting)
            if (target != null) {
                ConfigureUtil.configure(target, task)
            }
        }
    }

}
