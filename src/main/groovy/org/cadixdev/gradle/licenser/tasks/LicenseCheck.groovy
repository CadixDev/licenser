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

import org.cadixdev.gradle.licenser.LicenseViolationException
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask

class LicenseCheck extends LicenseTask implements VerificationTask {

    // We can't use a Groovy property, as it would produce 2 getters to
    // satisfy VerificationTask - which Gradle doesn't like.
    private boolean _ignoreFailures

    @Input
    @Override
    boolean getIgnoreFailures() {
        return this._ignoreFailures
    }

    @Override
    void setIgnoreFailures(boolean value) {
        this._ignoreFailures = value
    }

    @TaskAction
    void checkFiles() {
        didWork = false

        if (headers.size() == 1 && headers.first().text.empty) {
            return
        }

        Set<File> violations = []
        matchingFiles.visit { FileVisitDetails details ->
            if (!details.directory) {
                didWork = true
                def file = details.file

                try {
                    def prepared = prepareMatchingHeader(details, file)
                    if (prepared == null) {
                        return
                    }

                    if (!prepared.check(file, charset, skipExistingHeaders)) {
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
