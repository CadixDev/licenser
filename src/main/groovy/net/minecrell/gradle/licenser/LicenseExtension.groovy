package net.minecrell.gradle.licenser

import net.minecrell.gradle.licenser.header.HeaderFormatRegistry
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet

class LicenseExtension implements PatternFilterable {

    @Delegate
    final PatternSet patternSet = new PatternSet()

    File header

    boolean newLine = true

    Collection<SourceSet> sourceSets

    String charset = 'UTF-8'

    boolean ignoreFailures = false

    HeaderFormatRegistry style = new HeaderFormatRegistry()

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

    void style(Closure closure) {
        closure.delegate = style
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call(style)
    }

}
