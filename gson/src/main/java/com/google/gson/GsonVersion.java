package com.google.gson;

import java.io.InputStream;
import java.util.Properties;

/**
 * //Newly added file Provides a utility to retrieve the current version of the Gson library. The
 * version information is read from a properties file generated during the build process.
 */
public final class GsonVersion {

  private static final String VERSION;

  private GsonVersion() {
    // Prevent instantiation
  }

  static {
    String versionString = "unknown";
    try (InputStream in = GsonVersion.class.getResourceAsStream("/gson.properties")) {
      if (in != null) {
        Properties props = new Properties();
        props.load(in);
        versionString = props.getProperty("gson.version");
      }
    } catch (Exception e) {
      // Fails silently, version will remain "unknown". Or else will fail if logged messages
    }
    VERSION = versionString;
  }

  /**
   * Returns the version of the Gson library.
   *
   * @return the version string (e.g., "2.11.0-SNAPSHOT")
   */
  public static String getVersion() {
    return VERSION;
  }
}
