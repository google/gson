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
package com.google.gson.rest.query;

import java.util.List;

import com.google.gson.rest.definition.ID;
import com.google.gson.rest.definition.RestResource;

/**
 * A query for a list of rest resources.
 * 
 * @author Inderjeet Singh
 */
public interface ResourceQuery<I extends ID, R extends RestResource<I, R>, QUERY> {
  /**
   * Returns a list of resources matching the query
   */
  public List<R> query(QUERY query);
}
