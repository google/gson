# Gson Release Process

The following is a step-by-step procedure for releasing a new version of Google-Gson.

1. Go through all open bugs and identify which will be fixed in this release. Mark all others with an appropriate release tag. Identify duplicates, and close the bugs that will never be fixed. Fix all bugs for the release, and mark them fixed.
1. (obsolete step) Edit [`pom.xml`](pom.xml) and update the versions listed for Export-Package to the target version. Also add any new Java packages that have been introduced in Gson.
1. Ensure all changelists are code-reviewed and have +1
1. `cd gson` to the parent directory; ensure there are no open files and all changes are committed.
1. Run `mvn release:clean`
1. Do a dry run: `mvn release:prepare -DdryRun=true`
1. Start the release: `mvn release:prepare`
   * Answer questions: usually the defaults are fine.
   * This will do a full build, change version from `-SNAPSHOT` to the released version, commit and create the tags. It will then change the version to `-SNAPSHOT` for the next release.
1. Complete the release: `mvn release:perform`
1. [Log in to Nexus repository manager](https://oss.sonatype.org/index.html#welcome) at Sonatype and close the staging repository for Gson.
1. Download and sanity check all downloads. Do not skip this step! Once you release the staging repository, there is no going back. It will get synced with Maven central and you will not be able to update or delete anything. Your only recourse will be to release a new version of Gson and hope that no one uses the old one.
1. Release the staging repository for Gson. Gson will now get synced to Maven central with-in the next hour. For issues consult [Sonatype Guide](https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide#SonatypeOSSMavenRepositoryUsageGuide-8.ReleaseIt).

1. Update the version in the [Using Gson with Maven2 page](https://github.com/google/gson/blob/master/UserGuide.md#TOC-Gson-With-Maven)
1. Update [Gson Changelog](https://github.com/google/gson/blob/master/CHANGELOG.md). Also, look at all bugs that were fixed and add a few lines describing what changed in the release.
1. Create a post on the [Gson Discussion Forum](http://groups.google.com/group/google-gson)
1. Update the release version in [Wikipedia](http://en.wikipedia.org/wiki/GSON) and update the current "stable" release.

## Configuring a machine for deployment to Sonatype Repository

This section was borrowed heavily from [Doclava release process](http://code.google.com/p/doclava/wiki/ProcessRelease).

1. Install/Configure GPG following this [guide](http://www.sonatype.com/people/2010/01/how-to-generate-pgp-signatures-with-maven/).
1. [Create encrypted passwords](http://maven.apache.org/guides/mini/guide-encryption.html).
1. Create `~/.m2/settings.xml` similar to as described in [Doclava release process](https://code.google.com/p/doclava/wiki/ProcessRelease).
1. Now for deploying a snapshot repository, use `mvn deploy`.

## Getting Maven Publishing Privileges

Based on [Gson group thread](https://groups.google.com/d/topic/google-gson/DHWJHVFpIBg/discussion):

1. [Sign up for a Sonatype account](https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide) following instructions under (2) on that page
1. Ask one of the existing members of the repository to create a JIRA ticket (Step 3 of above document) to add you to the publisher list.

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
