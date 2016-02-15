# licenser
licenser is a simple license header manager for Gradle. It can automatically ensure that the source files contain a predefined license header and optionally generate them automatically using a Gradle task. It provides several options as configuration (e.g. variables in the license header, included file types, style of license header) so it can be customized for each project.

**Note:** licenser is still in **alpha**. It creates a backup of each file before applying the license header. Nevertheless, it is theoretically possible certain parts of the source code get lost when updating the license headers.

#### Differences to [license-gradle-plugin](https://github.com/hierynomus/license-gradle-plugin)
- **No dependencies (except Gradle)**: the license-gradle-plugin is based on the license-maven-plugin internally so it depends on some parts of the Maven distribution that need to be downloaded first
- **Less features, more lightweight**: licenser only offers the most common features needed for projects to stay simple and lightweight. The license-gradle-plugin provides more features with a certain overhead

## Features
- Apply pre-defined license header in a file to the source files of the source sets
- Apply license header only to certain files (include/exclude possible)
- Insert a blank new line after the license header
- Select the source sets to apply the license headers to
- Choose from a pre-defined set of license header styles and assign them to certain extensions
- **Maybe Soon(TM)**: Define custom license header styles

## Usage
For a simple project you only need to apply the licenser plugin to your project:

```gradle
plugins {
    id 'net.minecrell.licenser' version '0.1.1'
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
- **Toggle new empty line after the license header:** (Default: true)

    ```gradle
    license {
        newLine = false // Disables the new line
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
        
        // Ignore failures and only print a warning on license violations
        ignoreFailures = true
        
        // Read/write files with platform charset (Default: UTF-8)
        charset = Charset.defaultCharset().name()
    }
