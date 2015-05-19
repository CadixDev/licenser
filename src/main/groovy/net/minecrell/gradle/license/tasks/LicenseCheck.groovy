package net.minecrell.gradle.license.tasks

import net.minecrell.gradle.license.LicenseHeader
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask

class LicenseCheck extends LicenseTask implements VerificationTask {

    boolean ignoreFailures

    @TaskAction
    void checkFiles() {
        def header = this.header
        if (header.empty) {
            didWork = false
            return
        }

        files.visit { File file ->
            if (!file.directory) {
                if (!LicenseHeader.check(file, header)) {
                    throw new GradleException("License violations were found: $file.canonicalPath")
                }
            }
        }
    }

}
