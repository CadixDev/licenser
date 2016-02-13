package net.minecrell.gradle.licenser.tasks

import org.gradle.api.file.FileVisitDetails
import org.gradle.api.tasks.TaskAction

class LicenseUpdate extends LicenseTask {

    @TaskAction
    void formatFiles() {
        didWork = false

        def header = this.header
        if (header.text.empty) {
            return
        }

        files.visit { FileVisitDetails details ->
            if (!details.directory) {
                // TODO
            }
        }
    }

}
