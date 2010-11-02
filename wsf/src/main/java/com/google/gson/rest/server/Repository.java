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

import com.google.gson.rest.definition.HasId;
import com.google.gson.rest.definition.Id;

/**
 * An interface for a repository of rest resources. Meant for abstracting the server-side
 * storage of rest resources.
 *
 * @author inder
 *
 * @param <R> the type of rest resource
 */
public interface Repository<R extends HasId<R>> {
  public R get(Id<R> resourceId);

  /**
   * if resource.getId() == null, inserts the resource after assigning it a new id.
   * Otherwise, updates the resource ensuring that it pre-exists.
   */
  public R put(R resource);

  public void delete(Id<R> resourceId);
  
  public boolean exists(Id<R> resourceId);

  /**
   * Ensures that the specified resource has a valid id that will be used when it is saved
   */
  public Id<R> assignId(R resource);

  public Id<R> getNextId();
}
