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

package org.cadixdev.gradle.licenser.tasks

import org.gradle.api.GradleException
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.tasks.TaskAction

class LicenseUpdate extends LicenseTask {

    @TaskAction
    void formatFiles() {
        didWork = false

        if (headers.size() == 1 && headers.first().text.empty) {
            return
        }

        // Backup files before modifying them
        def original = new File(temporaryDir, 'original')
        def updated = 0
        def failed = false

        matchingFiles.visit { FileVisitDetails details ->
            if (!details.directory) {
                def file = details.file

                try {
                    def prepared = prepareMatchingHeader(details, file)
                    if (prepared == null) {
                        return
                    }

                    if (prepared.update(file, charset, skipExistingHeaders, {
                        def backup = details.relativePath.getFile(original)
                        if (backup.exists()) {
                            assert backup.delete(), "Failed to delete backup file: $backup"
                        } else {
                            backup.parentFile.mkdirs()
                        }

                        assert file.renameTo(backup), "Failed to backup file $file to $backup"
                        assert file.createNewFile(), "Failed to recreate source file: $file"
                    })) {
                        updated++
                        logger.lifecycle('Updating license header in {}', getSimplifiedPath(file))
                        didWork = true
                    }
                } catch (Exception e) {
                    logger.error("Failed to update license header in ${getSimplifiedPath(file)}", e)
                    failed = true
                }
            }
        }

        if (updated > 0) {
            logger.lifecycle('{} license header(s) updated. A backup of the original file(s) was created in {}', updated, getSimplifiedPath(original))
        }

        if (failed) {
            throw new GradleException('One or more license headers could not be successfully updated')
        }
    }

}
