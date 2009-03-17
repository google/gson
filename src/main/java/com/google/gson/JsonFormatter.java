/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.gson;

import java.io.IOException;

/**
 * Common interface for a formatter for Json. 
 * 
 * @author Inderjeet Singh
 */
interface JsonFormatter {

  /**
   * Writes a formatted version of the Json corresponding to 
   * the specified Json.  
   * 
   * @param root the root of the Json tree. 
   * @param writer the writer to output the formatter JSON to.
   * @param serializeNulls serialize null values in the output.
   */
  public void format(JsonElement root, Appendable writer, 
      boolean serializeNulls) throws IOException;
}
