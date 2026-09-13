# test-shrinker

This Maven module contains integration tests which check the behavior of Gson when used in combination with code shrinking and obfuscation tools, such as ProGuard or R8.

The code which is shrunken is under `src/main/java`; it should not contain any important assertions in case the code shrinking tools affect these assertions in any way. The test code under `src/test/java` executes the shrunken and obfuscated JAR and verifies that it behaves as expected.

The tests might be a bit brittle, especially the R8 test setup. Future ProGuard and R8 versions might cause the tests to behave differently. In case tests fail the ProGuard and R8 mapping files created in the `target` directory can help with debugging. If necessary rewrite tests or even remove them if they cannot be implemented anymore for newer ProGuard or R8 versions.

**Important:** Because execution of the code shrinking tools is performed during the Maven build, trying to directly run the integration tests from the IDE might not work, or might use stale results if you changed the configuration in between. Run `mvn clean verify` before trying to run the integration tests from the IDE.
