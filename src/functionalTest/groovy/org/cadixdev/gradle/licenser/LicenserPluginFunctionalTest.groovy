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

package org.cadixdev.gradle.licenser

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LicenserPluginFunctionalTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "can run licenseCheck task"() {
        given:
        def projectDir = temporaryFolder.newFolder()
        new File(projectDir, "settings.gradle") << ""
        new File(projectDir, "build.gradle") << """
            plugins {
                id('org.cadixdev.licenser')
            }
        """.stripIndent()

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("licenseCheck")
        runner.withProjectDir(projectDir)
        def result = runner.build()

        then:
        result.task(":checkLicenses").outcome == TaskOutcome.UP_TO_DATE
        result.task(":licenseCheck").outcome == TaskOutcome.UP_TO_DATE
    }

    def "can run licenseFormat task"() {
        given:
        def projectDir = temporaryFolder.newFolder()
        new File(projectDir, "settings.gradle") << ""
        new File(projectDir, "build.gradle") << """
            plugins {
                id('org.cadixdev.licenser')
            }
        """.stripIndent()

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("licenseFormat")
        runner.withProjectDir(projectDir)
        def result = runner.build()

        then:
        result.output.contains("Task :updateLicenses UP-TO-DATE")
        result.output.contains("Task :licenseFormat UP-TO-DATE")
        result.task(":updateLicenses").outcome == TaskOutcome.UP_TO_DATE
        result.task(":licenseFormat").outcome == TaskOutcome.UP_TO_DATE
    }

    def "supports custom source sets task"() {
        given:
        def projectDir = temporaryFolder.newFolder()
        new File(projectDir, "settings.gradle") << ""
        new File(projectDir, "build.gradle") << """
            plugins {
                id('org.cadixdev.licenser')
                id('java')
            }
            sourceSets {
                mySourceSet {}
            }
        """.stripIndent()

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("licenseCheck")
        runner.withProjectDir(projectDir)
        def result = runner.build()

        then:
        result.task(":checkLicenseMySourceSet").outcome == TaskOutcome.NO_SOURCE
    }

    def "supports custom style"() {
        given:
        def projectDir = temporaryFolder.newFolder()
        def sourcesDir = new File(projectDir, "sources")
        sourcesDir.mkdirs()
        new File(projectDir, "settings.gradle") << ""
        new File(projectDir, "header.txt") << "Copyright header"
        def sourceFile = new File(sourcesDir, "source.c") << "TEST"
        new File(projectDir, "build.gradle") << """
            plugins {
                id('org.cadixdev.licenser')
            }
            
            license {
                header = project.file("header.txt")
                style {
                    c = 'BLOCK_COMMENT'
                }
                tasks {
                    sources {
                        files = project.files("sources")
                        include("**/*.c")
                    }
                }
            }
        """.stripIndent()

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("updateLicenses")
        runner.withProjectDir(projectDir)
        def result = runner.build()

        then:
        result.task(":updateLicenseCustomSources").outcome == TaskOutcome.SUCCESS
        sourceFile.text == """\
            /*
             * Copyright header
             */
             
            TEST
            """.stripIndent()
    }

    def "supports Android source sets"() {
        given:
        def projectDir = temporaryFolder.newFolder()
        new File(projectDir, "settings.gradle") << ""
        new File(projectDir, "build.gradle") << """
            buildscript {
                repositories {
                    google()
                }
            
                dependencies {
                    classpath 'com.android.tools.build:gradle:4.0.0'
                }
            }

            plugins {
                id('org.cadixdev.licenser')
            }
            apply plugin: 'com.android.application'
            
            android {
                compileSdkVersion 30
                buildToolsVersion '30.0.1'
                defaultConfig {
                    applicationId 'org.gradle.samples'
                    minSdkVersion 16
                    targetSdkVersion 30
                    versionCode 1
                    versionName '1.0'
                    testInstrumentationRunner 'androidx.test.runner.AndroidJUnitRunner'
                }
            }
        """.stripIndent()

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("licenseCheck")
        runner.withProjectDir(projectDir)
        def result = runner.build()

        then:
        result.task(":checkLicenseAndroidMain").outcome == TaskOutcome.NO_SOURCE
        result.task(":checkLicenseAndroidRelease").outcome == TaskOutcome.NO_SOURCE
        result.task(":checkLicenseAndroidTest").outcome == TaskOutcome.NO_SOURCE
    }
}
