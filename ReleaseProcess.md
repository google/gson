# Gson Release Process

The following is a step-by-step procedure for releasing a new version of Google-Gson.

1. Go through all open bugs and identify which will be fixed in this release. Mark all others with an appropriate release tag. Identify duplicates, and close the bugs that will never be fixed. Fix all bugs for the release, and mark them fixed.
1. Ensure all changelists are code-reviewed and have +1
1. `cd gson` to the parent directory; ensure there are no open files and all changes are committed.
1. Run `mvn release:clean`
1. Start the release: `mvn release:prepare`
    - Answer questions: usually the defaults are fine. Try to follow [Semantic Versioning](https://semver.org/) when choosing the release version number.
    - This will do a full build, change version from `-SNAPSHOT` to the released version, commit and create the tags. It will then change the version to `-SNAPSHOT` for the next release.
1. Complete the release: `mvn release:perform`
1. [Log in to Nexus repository manager](https://oss.sonatype.org/index.html#welcome) at Sonatype and close the staging repository for Gson.
1. Download and sanity check all downloads. Do not skip this step! Once you release the staging repository, there is no going back. It will get synced with Maven Central and you will not be able to update or delete anything. Your only recourse will be to release a new version of Gson and hope that no one uses the old one.
1. Release the staging repository for Gson. Gson will now get synced to Maven Central with-in the next hour. For issues consult [Sonatype Guide](https://central.sonatype.org/publish/release/).
1. Create a [GitHub release](https://github.com/google/gson/releases) for the new version. You can let GitHub [automatically generate the description for the release](https://docs.github.com/en/repositories/releasing-projects-on-github/automatically-generated-release-notes), but you should edit it manually to point out the most important changes and potentially incompatible changes.
1. Update version references in (version might be referenced multiple times):
    - [`README.md`](README.md)
    - [`UserGuide.md`](UserGuide.md)

    Note: When using the Maven Release Plugin as described above, these version references should have been replaced automatically, but verify this manually nonetheless to be on the safe side.
1. Optional: Create a post on the [Gson Discussion Forum](https://groups.google.com/group/google-gson).
1. Optional: Update the release version in [Wikipedia](https://en.wikipedia.org/wiki/Gson) and update the current "stable" release.

Important: When aborting a release / rolling back release preparations, make sure to also revert all changes to files which were done during the release (e.g. automatic replacement of version references).

## Configuring a machine for deployment to Sonatype Repository

This section was borrowed heavily from [Doclava release process](https://code.google.com/archive/p/doclava/wikis/ProcessRelease.wiki).

1. Install/Configure GPG following this [guide](https://blog.sonatype.com/2010/01/how-to-generate-pgp-signatures-with-maven/).
1. [Create encrypted passwords](https://maven.apache.org/guides/mini/guide-encryption.html).
1. Create `~/.m2/settings.xml` similar to as described in [Doclava release process](https://code.google.com/p/doclava/wiki/ProcessRelease).
1. Now for deploying a snapshot repository, use `mvn deploy`.

## Getting Maven Publishing Privileges

See [OSSRH Publish Guide](https://central.sonatype.org/publish/publish-guide/).

## Testing Maven release workflow locally

The following describes how to perform the steps of the release locally to verify that they work as desired.

**Warning:** Be careful with this, these steps might be outdated or incomplete. Doublecheck that you are working on a copy of your local Gson Git repository and make sure you have followed all steps. To be safe you can also temporarily turn off your internet connection to avoid accidentally pushing changes to the real remote Git or Maven repository.\
As an alternative to the steps described below you can instead [perform a dry run](https://maven.apache.org/maven-release/maven-release-plugin/usage.html#do-a-dry-run), though this might not behave identical to a real release.

1. Make a copy of your local Gson Git repository and only work with that copy
2. Make sure you are on the `main` branch
3. Create a temp directory outside the Gson directory\
   In the following steps this will be called `#gson-remote-temp#`; replace this with the actual absolute file path of the directory, using only forward slashes. For example under Windows `C:\my-dir` becomes `C:/my-dir`.
4. Create the directory `#gson-remote-temp#/git-repo`
5. In that directory run

    ```sh
    git init --bare --initial-branch=main .
    ```

6. Create the directory `#gson-remote-temp#/maven-repo`
7. Edit the root `pom.xml` of Gson
    1. Change the `<developerConnection>` to

       ```txt
       scm:git:file:///#gson-remote-temp#/git-repo
       ```

    2. Change the `<url>` of the `<distributionManagement>` to

       ```txt
       file:///#gson-remote-temp#/maven-repo
       ```

    3. If you don't want to use GPG, remove the `maven-gpg-plugin` entry from the 'release' profile.\
       There is also an entry under `<pluginManagement>`; you can remove that as well.
8. Commit the changes using Git
9. Change the remote repository of the Git project

    <!-- Uses `txt` instead of `sh` to avoid the `#` being highlighted in some way -->
    ```txt
    git remote set-url origin file:///#gson-remote-temp#/git-repo
    ```

10. Push the changes

    ```sh
    git push origin main
    ```

Now you can perform the steps of the release:

1. ```sh
   mvn release:clean
   ```

2. ```sh
   mvn release:prepare
   ```

3. ```sh
   mvn release:perform
   ```

4. Verify that `#gson-remote-temp#/git-repo` and `#gson-remote-temp#/maven-repo` contain all the desired changes
5. Afterwards delete all Gson files under `${user.home}/.m2/repository/com/google/code/gson` which have been installed in your local Maven repository during the release.\
   Otherwise Maven might not download the real Gson artifacts with these version numbers, once they are released.

## Running Benchmarks or Tests on Android

* Download vogar
* Put `adb` on your `$PATH` and run:

  ```bash
  vogar --benchmark --classpath gson.jar path/to/Benchmark.java
  ```

For example, here is how to run the [CollectionsDeserializationBenchmark](gson/src/main/java/com/google/gson/metrics/CollectionsDeserializationBenchmark.java):

```bash
export ANDROID_HOME=~/apps/android-sdk-mac_x86
export PATH=$PATH:$ANDROID_HOME/platform-tools/:$ANDROID_HOME/android-sdk-mac_x86/tools/
$VOGAR_HOME/bin/vogar \
    --benchmark \
    --sourcepath ../gson/src/main/java/ \
    src/main/java/com/google/gson/metrics/CollectionsDeserializationBenchmark.java \
    -- \
    --vm "app_process -Xgc:noconcurrent,app_process"
```
