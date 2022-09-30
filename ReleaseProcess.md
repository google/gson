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
1. Update [Gson Changelog](CHANGELOG.md). Also, look at all bugs that were fixed and add a few lines describing what changed in the release.
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
