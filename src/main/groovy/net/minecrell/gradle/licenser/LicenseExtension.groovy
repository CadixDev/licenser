package net.minecrell.gradle.licenser

import net.minecrell.gradle.licenser.header.HeaderFormatRegistry
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet

/**
 * Represents the Gradle extension for configuring the settings for the
 * {@link Licenser} plugin.
 */
class LicenseExtension implements PatternFilterable {

    /**
     * The filter to apply to the source files of the {@link #sourceSets}.
     * By default this only includes a few excludes for binary files or files
     * without standardized comment formats.
     */
    @Delegate
    PatternFilterable filter = new PatternSet()

    /**
     * The path to the file containing the license header.
     * By default this is the {@code LICENSE} file in the project directory.
     */
    File header

    /**
     * Whether to insert an empty line after the license header.
     * By default this is {@code true}.
     */
    boolean newLine = true

    /**
     * The source sets to scan for files with license headers.
     * By default this includes all source sets of the project.
     */
    Collection<SourceSet> sourceSets

    /**
     * The charset to read/write the files with.
     * By default this is {@code UTF-8}.
     */
    String charset = 'UTF-8'

    /**
     * Whether to ignore failures and only warn about license violations
     * instead of failing the build.
     * By default this is {@code false}.
     */
    boolean ignoreFailures = false

    /**
     * The style mappings from file extension to the type of style of the
     * comment header for the license header.
     * By default this includes mappings and styles for the most common file
     * types.
     */
    HeaderFormatRegistry style = new HeaderFormatRegistry()

    /**
     * The (case-insensitive) keywords that identify a comment as license
     * header.
     * By default this includes only the words "Copyright" and "License".
     */
    List<String> keywords = ['Copyright', 'License']

    LicenseExtension() {
        // Files without standard comment format
        exclude '**/*.txt'
        exclude '**/*.json'
        exclude '**/*.md'

        // Image files
        exclude '**/*.jpg'
        exclude '**/*.png'
        exclude '**/*.gif'
        exclude '**/*.bmp'
        exclude '**/*.ico'

        // Manifest
        exclude '**/MANIFEST.MF'
        exclude '**/META-INF/services/**'
    }

    /**
     * Configures the license styles using the specified closure.
     *
     * @param closure The closure to apply to the style
     */
    void style(Closure closure) {
        closure.delegate = style
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call(style)
    }

}
