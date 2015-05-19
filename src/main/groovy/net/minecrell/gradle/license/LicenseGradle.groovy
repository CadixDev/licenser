package net.minecrell.gradle.license

import groovy.text.SimpleTemplateEngine
import net.minecrell.gradle.license.tasks.LicenseCheck
import net.minecrell.gradle.license.tasks.LicenseFormat
import net.minecrell.gradle.license.tasks.LicenseTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet

class LicenseGradle implements Plugin<Project> {

    private static final String CHECK_TASK = 'licenseCheck'
    private static final String FORMAT_TASK = 'licenseFormat'

    protected Project project
    protected LicenseExtension extension

    protected Task globalCheck
    protected Task globalFormat

    private List<String> header

    private final Closure<List<String>> headerLoader = {
        if (header != null) {
            return header
        }

        File header = extension.header
        if (header != null && header.exists()) {
            String text

            Map<String, String> properties = extension.ext.properties
            if (properties != null && !properties.isEmpty()) {
                def engine = new SimpleTemplateEngine()
                def template = engine.createTemplate(header).make(properties)
                text = template.toString()
            } else {
                text = header.text
            }

            return this.header = addComment(text)
        }

        this.header = []
    }

    @Override
    void apply(Project project) {
        this.project = project

        project.with {
            this.extension = extensions.create('license', LicenseExtension)
            extension.with {
                header = project.file('LICENSE')
                ignoreFailures = false
                sourceSets = project.sourceSets
            }

            this.globalCheck = task(CHECK_TASK)
            this.globalFormat = task(FORMAT_TASK)

            // Wait a bit until creating the tasks
            afterEvaluate {
                extension.sourceSets.each {
                    def check = createTask(it.getTaskName(CHECK_TASK, null), LicenseCheck, it)
                    check.ignoreFailures = extension.ignoreFailures
                    globalCheck.dependsOn check
                    globalFormat.dependsOn createTask(it.getTaskName(FORMAT_TASK, null), LicenseFormat, it)
                }
            }
        }
    }

    Project getProject() {
        project
    }

    LicenseExtension getExtension() {
        extension
    }

    List<String> getHeader() {
        return headerLoader.call()
    }

    private static List<String> addComment(String text) {
        def result = ['/*']

        text.eachLine {
            result << " * $it"
        }

        result << ' */' << ''
    }

    private <T extends LicenseTask> T createTask(String name, Class<T> type, SourceSet sourceSet) {
        (T) project.task(name, type: type) {
            it.header = this.headerLoader
            it.files = sourceSet.allSource
            it.filter = extension
        }
    }

}
