package com.google.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * A dedicated test for the GsonVersion utility class to ensure it correctly reads the version from
 * the build-processed properties file.
 */
public class GsonVersionTest {

  @Test
  public void testGetVersion() {
    // This is the line that runs your code
    // If GsonVersion shows error is normal
    String version = GsonVersion.getVersion();

    // These assertions prove to Maven that the test passed.
    assertNotNull("Version string should not be null", version);
    assertNotEquals("Version string should have been replaced by Maven", "unknown", version);

    // This checks if the version is the one we expect from the pom.xml
    assertEquals("Version should match the project's version", "2.13.2-SNAPSHOT", version);
  }
}
