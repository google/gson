# shrinker-test

This Maven module contains integration tests which check the behavior of Gson when used in combination with code shrinking and obfuscation tools, such as ProGuard or R8.

The code which is shrunken is under `src/main/java`; it should not contain any important assertions in case the code shrinking tools affect these assertions in any way. The test code under `src/test/java` executes the shrunken and obfuscated JAR and verifies that it behaves as expected.

**Important:** Because execution of the code shrinking tools is performed during the Maven build, trying to directly run the integration tests from the IDE might not work, or might use stale results if you changed the configuration in between. Run `mvn clean verify` before trying to run the integration tests from the IDE.
