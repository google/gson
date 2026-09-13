# test-graal-native-image

This Maven module contains integration tests for using Gson in a GraalVM Native Image.

Execution requires using GraalVM as JDK, and can be quite resource intensive. Native Image tests are therefore not enabled by default and the tests are only executed as regular unit tests. To run Native Image tests, make sure your `PATH` and `JAVA_HOME` environment variables point to GraalVM and then run:

```
mvn clean test --activate-profiles native-image-test
```

Technically it would also be possible to directly configure Native Image test execution for the `gson` module instead of having this separate Maven module. However, maintaining the reflection metadata for the unit tests would be quite cumbersome and would hinder future changes to the `gson` unit tests because many of them just happen to use reflection, without all of them being relevant for Native Image testing.

## Reflection metadata

Native Image creation requires configuring which class members are accessed using reflection, see the [GraalVM documentation](https://www.graalvm.org/jdk21/reference-manual/native-image/metadata/#specifying-reflection-metadata-in-json).

The file [`reflect-config.json`](./src/test/resources/META-INF/native-image/reflect-config.json) contains this reflection metadata.

You can also run with `-Dagent=true` to let the Maven plugin automatically generate a metadata file, see the [plugin documentation](https://graalvm.github.io/native-build-tools/latest/maven-plugin.html#agent-support-running-tests).
