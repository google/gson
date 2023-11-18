/*
 * Copyright (C) 2021 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.metrics;

import com.google.caliper.runner.CaliperMain;

class NonUploadingCaliperRunner {
  private static String[] concat(String first, String... others) {
    if (others.length == 0) {
      return new String[] {first};
    } else {
      String[] result = new String[others.length + 1];
      result[0] = first;
      System.arraycopy(others, 0, result, 1, others.length);
      return result;
    }
  }

  public static void run(Class<?> c, String[] args) {
    // Disable result upload; Caliper uploads results to webapp by default, see
    // https://github.com/google/caliper/issues/356
    CaliperMain.main(c, concat("-Cresults.upload.options.url=", args));
  }
}
