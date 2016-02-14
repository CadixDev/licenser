package net.minecrell.gradle.licenser.tasks

import net.minecrell.gradle.licenser.LicenseViolationException
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask

class LicenseCheck extends LicenseTask implements VerificationTask {

    boolean ignoreFailures

    @TaskAction
    void checkFiles() {
        didWork = false

        def header = this.header
        if (header.text.empty) {
            return
        }

        Set<File> violations = []
        files.visit { FileVisitDetails details ->
            if (!details.directory) {
                didWork = true

                def file = details.file
                def prepared = this.header.prepare(file)
                if (prepared == null) {
                    logger.warn("No matching header format found for file {}", getSimplifiedPath(file))
                    return
                }

                if (!prepared.check(file, charset)) {
                    violations.add(file)
                }
            }
        }

        if (!violations.isEmpty()) {
            String violators = violations.collect { getSimplifiedPath(it) }.join(', ')

            final def message = "License violations were found: $violators"
            if (ignoreFailures) {
                logger.error(message)
            } else {
                throw new LicenseViolationException(message)
            }
        }
    }

}
