## Issues

- Before submitting a bug-related issue, make sure that **its source is not the LibGDX itself**.
- Issues are among the fastest way to contact the developers. It is fine to create issues with feature requests and
questions about the library.

## Pull requests

- The latest changes are always in the `develop` branch. `master` branch always matches the latest stable release. Make
sure to checkout `develop` branch before starting your work and set `develop` as the target branch before creating
a pull request.
- Use IntelliJ Kotlin [code formatter](../intellij-formatter.xml) included in the root directory.
- Make sure to include unit tests of your code. Test names should use the `` `backtick method name` `` syntax.
JUnit and [Spek](http://spekframework.org/) can be used to write tests. Use
[Mockito-Kotlin](https://github.com/nhaarman/mockito-kotlin) for mocking.
- If your pull request is not a simple bug fix or small utility, make sure to link it to an existing issue or create
an issue with your proposal first. Major API changes or new modules have to be discussed with the maintainers first.
- All notable changes should be added to the [changelog](../CHANGELOG.md) with an appropriate label:
  - **[FEATURE]** - a new functionality.
  - **[CHANGE]** - breaking API change.
  - **[UPDATE]** - update of one of the dependencies.
  - **[FIX]** - bug fix.
  - **[MISC]** - other changes (e.g. related to documentation or the project itself).
- Some libraries (like `ktx-collections` or `ktx-math`) list _all_ features in the `README.md` files to ease their
usage. When adding new feature to these projects, please add description of your change to the file. Also, add all
necessary imports in KTX examples in `README.md` files to make it easier to try them out.

## Working from sources

```bash
git clone https://github.com/libktx/ktx.git
cd ktx
git checkout develop
```

### Build tool

The project itself is managed by [Gradle](http://gradle.org/). Gradle wrapper is included, but you can use a local
Gradle installation - scripts should be compatible with Gradle `4.+`. If you consider working from sources, these are
some useful Gradle tasks that you can look into:

- `build install` - builds the libraries archives and pushes them to _Maven Local_.
- `check` - runs all tests in all projects.
- `clean` - removes `build` directories.
- `distZip` - prepares a zip archive with all jars in `build/distributions` folder. Useful for releases.
- `uploadArchives` - pushes the archives to _Maven Central_. Requires proper `gradle.properties` with archive signing and
Sonatype logging data.
- `closeAndPromoteRepository` - closes and promotes Nexus repository. Should be run after `uploadArchives` in
case of a non-snapshot upload to _Maven Central_.

### Versioning and uploading

Releasing a new KTX version:

- Change `libVersion` settings in `version.txt`. KTX uses the same versioning schema as LibGDX (mimicking the
LibGDX version that it was compiled against) with optional `-b#` or `-SNAPSHOT` suffixes depending on version status.
- Run `gradle build install uploadArchives closeAndPromoteRepository` to push artifacts to both _Maven Local_ and
_Maven Central_. Note that Maven plugin has its issues and you might need to run `gradle promoteRepository` after the
previous task sequence (if it fails on the `closeAndPromoteRepository` task).
- Run `gradle distZip` to prepare archive with KTX sources, compiled binary and documentation.
- Upload the archive to [releases](https://github.com/libktx/ktx/releases) section. Tag should match released version.
Name of the release should match `KTX $libVersion`. Copy latest [changelog](../CHANGELOG.md) entries to release
description. Note that a release is not necessary for snapshot versions. If there are any known issues with the previous
or current versions, please attach additional _Known issues:_ section with the following labels:
  - **[BUG]** - a known bug in the release that is or will be fixed in the following versions.
  - **[INCOMPATIBILITY]** - incompatibility with one of the previously supported or currently released versions of one
  of the major dependencies.
  - **[REMOVAL]** - temporary or permanent removal of a major feature (e.g. disabling a module for a single release).

### Updating dependencies

Dependencies versions are stored in the [gradle.properties](../gradle.properties) file. Snapshot releases should keep
all of the dependencies (outside of testing scope) up-to-date. Major dependencies updating:

- **LibGDX**: update `gdxVersion` in the properties file and LibGDX version in the tag on the top of the 
[README.md](../README.md) file. Note that updating LibGDX also affects the KTX version, so make sure to update
[version.txt](../version.txt) as well.
- **Kotlin**: update the `kotlinVersion` property and the Kotlin tag in the [README.md](../README.md).
- **Kotlin Coroutines**: update `kotlinCoroutinesVersion` property and the tag in the
`ktx-async` [README.md](../async/README.md).
- **Gradle**: run `gradle wrapper` in the root project folder. Make sure that the
[Gradle wrapper properties file](../gradle/wrapper/gradle-wrapper.properties) points the `all` Gradle release under
`distributionUrl` rather than just the binaries (`bin`).
- **VisUI**: update `visUiVersion` in the properties file and VisUI version in the tag on the top of the 
[vis/README.md](../vis/README.md) file.

All of the major dependencies updates should be added to the [changelog](../CHANGELOG.md).

### Adding a new KTX module

Adding a new library to KTX:
- Create folder matching module name in root of the repository. Modules should generally be named with a single word;
if using multiple words, use dash (`-`) as separator.
- Add folder name to `settings.gradle` file. This will also server as the project identifier that you use in
`build.gradle` scripts and to run individual Gradle tasks (like `gradle actors:test`).
- Create `src/main/kotlin` and `src/test/kotlin` directories in your module folder. They will be automatically marked
as source thanks to Gradle. You should also create package structure matching `ktx/your/module` in each source folder.
- Add `gradle.properties` file with the following properties:

```properties
projectName=ktx-your-module
projectDesc=Description of your module as it will appear in Maven Central.
```

- Add `build.gradle` file. It should contain dependencies specific to your module. If there are none, you can leave it
empty.
- Add `README.md` file describing your module. Refer to other `README.md` files for guidelines.
- Add short description of the module to root `README.md`.
- Your final module structure should roughly match this schema:

```
> your-module/
  > src/
    > main/kotlin/ktx/your/module/
      > yourModule.kt
    > test/kotlin/ktx/your/module/
      > yourModuleTest.kt
  > build.gradle
  > gradle.properties
  > README.md
```
