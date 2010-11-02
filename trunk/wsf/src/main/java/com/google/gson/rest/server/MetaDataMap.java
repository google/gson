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

import java.util.HashMap;
import java.util.Map;

import com.google.gson.rest.definition.Id;
import com.google.gson.rest.definition.MetaData;
import com.google.gson.rest.definition.RestResource;

/**
 * A map of resources to their MetaData
 *
 * @author inder
 *
 * @param <R> the rest resource for whic the metadata is being stored
 */
public class MetaDataMap<R extends RestResource<R>> {
  private final Map<Id<R>, MetaData<R>> map;

  public MetaDataMap() {
    this.map = new HashMap<Id<R>, MetaData<R>>();
  }

  public MetaData<R> get(Id<R> resourceId) {
    MetaData<R> metaData = map.get(resourceId);
    if (metaData == null) {
      metaData = MetaData.create();
      map.put(resourceId, metaData);
    }
    return metaData;
  }

  @Override
  public String toString() {
    return String.format("%s", map);
  }
}
