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
package com.google.gson.wsf.server.rest;

import com.google.common.base.Preconditions;
import com.google.gson.webservice.definition.rest.Id;
import com.google.gson.webservice.definition.rest.MetaData;
import com.google.gson.webservice.definition.rest.RestResource;

/**
 * An in-memory map of rest resources
 *
 * @author inder
 *
 * @param <R> Type variable for the resource
 */
public class RepositoryInMemory<R extends RestResource<R>> implements Repository<R> {
  private static final String METADATA_KEY_IS_FRESHLY_ASSIGNED_ID = "isFreshlyAssignedId";

  private final IdMap<R> resources;
  private final MetaDataMap<R> metaDataMap;

  public RepositoryInMemory(Class<? super R> classOfResource) {
    this.resources = IdMap.create(classOfResource);
    this.metaDataMap = new MetaDataMap<R>();
  }

  @Override
  public R get(Id<R> resourceId) {
    return resources.get(resourceId);
  }

  public boolean isFreshlyAssignedId(Id<R> resourceId) {
    MetaData<R> metaData = metaDataMap.get(resourceId);
    if (metaData == null) {
      return false;
    }
    return metaData.getBoolean(METADATA_KEY_IS_FRESHLY_ASSIGNED_ID);
  }

  @Override
  public R put(R resource) {
    if (!resource.hasId()) {
      // insert semantics
      assignId(resource);
    } else {
      Id<R> id = resource.getId();
      if (!isFreshlyAssignedId(id)) {
        // update semantics
        Preconditions.checkState(resources.exists(resource.getId()));
      }
    }
    resource = resources.put(resource);
    metaDataMap.get(resource.getId()).remove(METADATA_KEY_IS_FRESHLY_ASSIGNED_ID);
    return resource;
  }

  @Override
  public void delete(Id<R> resourceId) {
    resources.delete(resourceId);
  }

  @Override
  public boolean exists(Id<R> resourceId) {
    return resources.exists(resourceId);
  }

  @Override
  public Id<R> getNextId() {
    return resources.getNextId();
  }

  @Override
  public Id<R> assignId(R resource) {
    if (resource.getId() == null) {
      Id<R> id = resources.getNextId();
      resource.setId(id);
      metaDataMap.get(id).putBoolean(METADATA_KEY_IS_FRESHLY_ASSIGNED_ID, true);
    }
    return resource.getId();
  }
}
