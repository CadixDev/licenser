package net.minecrell.gradle.licenser.tasks

import net.minecrell.gradle.licenser.header.Header
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.util.PatternFilterable

class LicenseTask extends DefaultTask {

    @Input
    Header header

    @InputFiles
    @SkipWhenEmpty
    FileCollection files

    PatternFilterable filter

    String charset

    @Input
    String getFormattedHeader() {
        return header.text
    }

    FileTree getMatchingFiles() {
        return this.files.asFileTree.matching(filter)
    }

    private String projectPath

    protected String getSimplifiedPath(File file) {
        if (projectPath == null) {
            projectPath = project.projectDir.canonicalPath
        }

        def path = file.canonicalPath
        return path.startsWith(projectPath) ? path[projectPath.length()+1..-1] : path
    }

}
