package net.minecrell.gradle.license.tasks

import net.minecrell.gradle.license.LicenseHeader
import org.gradle.api.tasks.TaskAction

class LicenseFormat extends LicenseTask {

    @TaskAction
    void formatFiles() {
        def header = this.header
        if (header.empty) {
            didWork = false
            return
        }

        files.visit { File file ->
            if (!file.directory) {
                if (!LicenseHeader.check(file, header)) {
                    // Update license header
                }
            }
        }
    }

}
