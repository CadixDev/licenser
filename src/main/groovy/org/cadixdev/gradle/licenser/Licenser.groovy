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

import static org.gradle.api.plugins.JavaBasePlugin.CHECK_TASK_NAME

import groovy.text.SimpleTemplateEngine
import org.cadixdev.gradle.licenser.header.Header
import org.cadixdev.gradle.licenser.tasks.LicenseCheck
import org.cadixdev.gradle.licenser.tasks.LicenseTask
import org.cadixdev.gradle.licenser.tasks.LicenseUpdate
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.util.PatternSet

class Licenser implements Plugin<Project> {

    private static final String CHECK_TASK = 'checkLicense'
    private static final String FORMAT_TASK = 'updateLicense'
    private static final String ANDROID_TASK = 'Android'
    private static final String CUSTOM_TASK = 'Custom'

    private Project project
    private LicenseExtension extension

    @Override
    void apply(Project project) {
        this.project = project

        project.with {
            this.extension = extensions.create('license', LicenseExtension, project)
            extension.header = project.file('LICENSE')
            plugins.withType(JavaBasePlugin) {
                extension.sourceSets = project.sourceSets
            }

            ['com.android.library', 'com.android.application'].each {
                plugins.withId(it) {
                    extension.androidSourceSets = android.sourceSets
                }
            }

            def globalCheck = task(CHECK_TASK + 's')
            task('licenseCheck', dependsOn: globalCheck)
            def globalFormat = task(FORMAT_TASK + 's')
            task('licenseFormat', dependsOn: globalFormat)

            afterEvaluate {
                project.tasks.findByName(CHECK_TASK_NAME)?.dependsOn globalCheck
            }

            // Wait a bit until creating the tasks
            afterEvaluate {
                def headers = []
                extension.conditionalProperties.reverseEach {
                    headers << prepareHeader(extension, it)
                }
                headers << prepareHeader(extension, extension)

                extension.sourceSets.each {
                    def check = createTask(CHECK_TASK, LicenseCheck, headers, it)
                    check.ignoreFailures = extension.ignoreFailures
                    globalCheck.dependsOn check
                    globalFormat.dependsOn createTask(FORMAT_TASK, LicenseUpdate, headers, it)
                }

                extension.androidSourceSets.each {
                    def check = createAndroidTask(CHECK_TASK, LicenseCheck, headers, it)
                    check.ignoreFailures = extension.ignoreFailures
                    globalCheck.dependsOn check
                    globalFormat.dependsOn createAndroidTask(FORMAT_TASK, LicenseUpdate, headers, it)
                }

                extension.tasks.each {
                    def check = createCustomTask(CHECK_TASK, LicenseCheck, it)
                    check.ignoreFailures = extension.ignoreFailures
                    globalCheck.dependsOn check
                    globalFormat.dependsOn createCustomTask(FORMAT_TASK, LicenseUpdate, it)
                }
            }
        }
    }

    private static Header prepareHeader(LicenseExtension extension, LicenseProperties properties) {
        return new Header(extension.style, extension.keywords, {
            File header = properties.header ?: extension.header
            if (header != null && header.exists()) {
                def text = header.getText(extension.charset)

                Map<String, String> props = extension.ext.properties
                if (props) {
                    def engine = new SimpleTemplateEngine()
                    def template = engine.createTemplate(text).make(props)
                    text = template.toString()
                }

                return text
            }

            return ""
        }, (PatternSet) properties.filter, properties.newLine ?: extension.newLine,)
    }

    private <T extends LicenseTask> T createTask(String name, Class<T> type, List<Header> headers, SourceSet sourceSet) {
        return makeTask(sourceSet.getTaskName(name, null), type, headers, sourceSet.allSource)
    }

    private <T extends LicenseTask> T createAndroidTask(String name, Class<T> type, List<Header> headers, Object sourceSet) {
        return makeTask(name + ANDROID_TASK + sourceSet.name.capitalize(), type, headers,
                project.files(sourceSet.java.sourceFiles, sourceSet.res.sourceFiles, sourceSet.manifest.srcFile))
    }

    private <T extends LicenseTask> T createCustomTask(String name, Class<T> type, LicenseTaskProperties properties) {
        def headers = [prepareHeader(extension, properties)]
        def task = makeTask(name + CUSTOM_TASK + properties.name.capitalize(), type, headers, properties.files)
        task.filter = properties.filter
        return task

    }

    private <T extends LicenseTask> T makeTask(String name, Class<T> type, List<Header> headers, FileCollection files) {
        return (T) project.task(name, type: type) { T task ->
            task.headers = headers
            task.files = files
            task.filter = extension.filter
            task.charset = extension.charset
            task.skipExistingHeaders = extension.skipExistingHeaders
        }
    }

}
