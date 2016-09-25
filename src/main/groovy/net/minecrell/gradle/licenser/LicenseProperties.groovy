package net.minecrell.gradle.licenser

import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet

/**
 * Specifies how to format the license header of a subset of the files, defined
 * by the {@link #filter}.
 */
class LicenseProperties implements PatternFilterable {

    /**
     * The filter to apply to the source files.
     * By default this only includes a few excludes for binary files or files
     * without standardized comment formats.
     */
    @Delegate
    PatternFilterable filter

    /**
     * The path to the file containing the license header.
     * By default this is the {@code LICENSE} file in the project directory.
     */
    File header

    /**
     * Whether to insert an empty line after the license header.
     * By default this is {@code true}.
     */
    Boolean newLine

    LicenseProperties(PatternSet filter) {
        this.filter = filter
    }

}
