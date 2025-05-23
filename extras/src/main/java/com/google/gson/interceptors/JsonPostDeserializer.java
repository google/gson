/*
 * Copyright (C) 2012 Google Inc.
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
package com.google.gson.interceptors;

import com.google.gson.InstanceCreator;

/**
 * This interface is implemented by a class that wishes to inspect or modify an object after it has
 * been deserialized. You must define a no-args constructor or register an {@link InstanceCreator}
 * for such a class.
 *
 * @author Inderjeet Singh
 */
public interface JsonPostDeserializer<T> {

  /** This method is called by Gson after the object has been deserialized from Json. */
  public void postDeserialize(T object);
}
