package net.minecrell.gradle.license

import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.util.PatternSet

class LicenseExtension extends PatternSet {

    File header

    Collection<SourceSet> sourceSets

    boolean ignoreFailures

}
