/*
 * Licenser
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
        matchingFiles.visit { FileVisitDetails details ->
            if (!details.directory) {
                didWork = true
                def file = details.file

                try {
                    def prepared = this.header.prepare(file)
                    if (prepared == null) {
                        logger.warn('No matching header format found for {}', getSimplifiedPath(file))
                        return
                    }

                    if (!prepared.check(file, charset)) {
                        violations.add(file)
                    }
                } catch (Exception e) {
                    violations.add(file)
                    logger.error("Failed to check license header of ${getSimplifiedPath(file)}", e)
                }
            }
        }

        if (!violations.isEmpty()) {
            String violators = violations.collect { getSimplifiedPath(it) }.join(', ')

            final def message = "License violations were found: $violators"
            if (ignoreFailures) {
                logger.warn(message)
            } else {
                throw new LicenseViolationException(message)
            }
        }
    }

}
