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

        // Backup files before modifying them
        def original = new File(temporaryDir, 'original')
        files.visit { FileVisitDetails details ->
            if (!details.directory) {
                def file = details.file
                def prepared = this.header.prepare(file)
                if (prepared == null) {
                    logger.warn("No matching header format found for file: {}", getSimplifiedPath(file))
                    return
                }

                if (prepared.update(file, charset, {
                    def backup = details.relativePath.getFile(original)
                    if (backup.exists()) {
                        assert backup.delete(), 'Failed to delete backup file %backup'
                    } else {
                        backup.parentFile.mkdirs()
                    }

                    assert file.renameTo(backup), "Failed to backup file $file to $backup"
                    assert file.createNewFile(), "Failed to recreate source file $file"
                })) {
                    logger.lifecycle('Updating license header in: {}', getSimplifiedPath(file))
                    didWork = true
                }
            }
        }
    }

}
