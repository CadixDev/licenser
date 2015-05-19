package net.minecrell.gradle.license.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.util.PatternFilterable

class LicenseTask extends DefaultTask {

    @Input
    Closure<List<String>> header;

    @InputFiles
    @SkipWhenEmpty
    FileCollection files

    PatternFilterable filter

    List<String> getHeader() {
        return header.call()
    }

    FileTree getFiles() {
        FileTree files = this.files.asFileTree
        files.matching(filter)
    }

}
