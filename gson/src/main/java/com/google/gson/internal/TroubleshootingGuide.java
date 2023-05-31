package com.google.gson.internal;

public class TroubleshootingGuide {
  private TroubleshootingGuide() {}

  /**
   * Creates a URL referring to the specified troubleshooting section.
   */
  public static String createUrl(String id) {
    return "https://github.com/google/gson/blob/master/Troubleshooting.md#" + id;
  }
}
