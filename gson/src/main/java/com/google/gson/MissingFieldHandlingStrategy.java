/*
 * Copyright (C) 2017 Google Inc.
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

import com.google.gson.reflect.TypeToken;

/**
 * A strategy that is used to determine the value to assign to the field whose node was
 * missing in Json.
 * This strategy is implemented by the user and should be passed to
 * {@link GsonBuilder#useMissingFieldHandlingStrategy(MissingFieldHandlingStrategy)} method
 * so that Gson can use it.
 * The {@link #handle(TypeToken, String)} is used by the Gson to determine the default value
 * for a field based on the {@link TypeToken} and fieldName passed.
 *
 * @author Prateek Jain
 */
public interface MissingFieldHandlingStrategy {
  Object handle(TypeToken type, String fieldName);
}
