# licenser
licenser is a simple license header manager for Gradle. It can automatically ensure that the source files contain a predefined license header and optionally generate them automatically using a Gradle task. It provides several options as configuration (e.g. variables in the license header, included file types, style of license header) so it can be customized for each project.

## Features
- Apply pre-defined license header in a file to the source files of the source sets or any other set of files (configurable)
- Variable substitution in the license header file
- Apply license header only to certain files (include/exclude possible)
- Apply special license headers to matching files  
- Support for Android projects

## Usage
For a simple project you only need to apply the licenser plugin to your project:

```gradle
plugins {
    id 'org.cadixdev.licenser' version '0.5.0'
}
```

This will apply the `LICENSE` file found in the project directory to all common source file types known to licenser.


## Tasks
|Name|Description|
|----|-----------|
|`checkLicenses`|Verifies the license headers for the selected source files.|
|`updateLicenses`|Updates the license headers in the selected source files. Will create a backup in `build/tmp/updateLicense<SourceSet>/original`.|
|`checkLicense<SourceSet>`|Verifies the license headers for the specified source set.|
|`updateLicense<SourceSet>`|Updates the license headers in the specified source set. Will create a backup in `build/tmp/updateLicense<SourceSet>/original`.|
|`checkLicenseAndroid<SourceSet>`|Same as `checkLicense<SourceSet>`, but for Android source sets.|
|`updateLicenseAndroid<SourceSet>`|Same as `updateLicense<SourceSet>`, but for Android source sets.|
|`checkLicenseCustom<Name>`|Same as `checkLicense<SourceSet>`, but for custom tasks.|
|`updateLicenseCustom<Name>`|Same as `updateLicense<SourceSet>`, but for custom tasks.|
|`licenseCheck`|Alias for `checkLicenses`|
|`licenseFormat`|Alias for `updateLicenses`|

## Configuration
The plugin can be configured using the `license` extension on the project.

- **Custom header file source:** (Default: `LICENSE` in project directory)

    ```gradle
    license {
        header = project.file('HEADER.txt')
    }
    ```

- **Variable substitution in the license header:**

    ```gradle
    license {
        header = project.file('HEADER.txt')
        ext {
            name = 'Company'
            year = 2018
        }

        // Example license header: Copyright (C) ${year} ${name}
    }
    ```

- **Toggle new empty line after the license header:** (Default: true)

    ```gradle
    license {
        newLine = false // Disables the new line
    }
    ```
- **Ignore existing license headers:** (Default: false)

    ```gradle
    license {
        skipExistingHeaders = true // Ignore existing license headers on files
    }
    ```
- **Exclude/include certain file types:** (Default: Excludes files without standard comment format and binary files)

    ```gradle
    license {
        include '**/*.java' // Apply license header ONLY to Java files
        // OR
        exclude '**/*.properties' // Apply license header NOT to properties files
    }
    ```
- **Apply special license header to some matching files:**

    ```gradle
    license {
        // Apply special license header to one source file
        matching('**/ThirdPartyLibrary.java') {
            header = file('THIRDPARTY-LICENSE.txt')
        }
        
        // Apply special license header to matching source files
        matching(includes: ['**/thirdpartylibrary/**', '**/ThirdPartyLibrary.java']) {
            header = file('THIRDPARTY-LICENSE.txt')
        }
    }
    ```
- **Custom tasks: Apply license header to files outside of source set:**

    ```gradle
    license {
        tasks {
            gradle {
                files = project.files('build.gradle.kts', 'settings.gradle.kts', 'gradle.properties')
                // header = ... (optional)
            }
            directory {
                files = project.files('path/to/directory')
                // include/exclude ... (optional)
            }
        }
    }
    ```

- **Manage file extension to license header styles:**

    ```gradle
    license {
        style {
            java = 'JAVADOC' // Sets Java license header style to JAVADOC (/**)
        }
    }
    ```
- **Other options:**

    ```gradle
    license {
        // Apply licenses only to main source set
        sourceSets = [project.sourceSets.main]

        // Apply licenses only to certain source sets on Android
        androidSourceSets = [android.sourceSets.main]

        // Ignore failures and only print a warning on license violations
        ignoreFailures = true

        // Read/write files with platform charset (Default: UTF-8)
        charset = Charset.defaultCharset().name()
    }
