/*
 * Copyright (C) 2010 Google Inc.
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
package com.google.gson.rest.server;

import com.google.greaze.definition.rest.HasId;
import com.google.greaze.definition.rest.ID;

/**
 * An interface for a repository of rest resources. Meant for abstracting the server-side
 * storage of rest resources.
 *
 * @author inder
 *
 * @param <R> the type of rest resource
 */
public interface Repository<I extends ID, R extends HasId<I>> {
  public R get(I resourceId);

  /**
   * if resource.getId() == null, inserts the resource after assigning it a new id.
   * Otherwise, updates the resource ensuring that it pre-exists.
   */
  public R put(R resource);

  public void delete(I resourceId);
  
  public boolean exists(I resourceId);

  /**
   * Ensures that the specified resource has a valid id that will be used when it is saved
   */
  public I assignId(R resource);

  public I getNextId();
}
