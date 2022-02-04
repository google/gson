package com.google.gson.metrics;

import com.google.caliper.runner.CaliperMain;

class NonUploadingCaliperRunner {
  private static String[] concat(String first, String... others) {
    if (others.length == 0) {
      return new String[] { first };
    } else {
      String[] result = new String[others.length + 1];
      result[0] = first;
      System.arraycopy(others, 0, result, 1, others.length);
      return result;
    }
  }

  public static void run(Class<?> c, String[] args) {
    // Disable result upload; Caliper uploads results to webapp by default, see https://github.com/google/caliper/issues/356
    CaliperMain.main(c, concat("-Cresults.upload.options.url=", args));
  }
}
