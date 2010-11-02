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

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.rest.definition.HasId;
import com.google.gson.rest.definition.Id;

/**
 * This class provides a type-safe map to access values associated with Ids
 *
 * @author inder
 *
 * @param <T> the type of the objects being kept in the map
 */
public class IdMap<T extends HasId<T>> {
  public static final Logger LOG = Logger.getLogger(IdMap.class.getName());
  protected final Map<Id<T>, T> map;
  private volatile long nextAvailableId;
  private final Type typeOfId;

  /**
   * Use {@link #create(Type)} instead of constructor
   */
  protected IdMap(Type typeOfId) {
    this.typeOfId = typeOfId;
    map = new ConcurrentHashMap<Id<T>, T>();
    nextAvailableId = 0;
  }

  public T get(Id<T> id) {
    return map.get(id);
  }

  public T put(T obj) {
    map.put(obj.getId(), obj);
    return obj;
  }

  public void delete(Id<T> id) {
    T removed = map.remove(id);
    if (removed == null) {
      LOG.log(Level.WARNING, "Attempted to delete non-existent id: {0}", id);
    }
  }

  public boolean exists(Id<T> id) {
    return map.containsKey(id);
  }

  public synchronized Id<T> getNextId() {
    long id = nextAvailableId++;
    return Id.get(id, typeOfId);
  }

  public static <S extends HasId<S>> IdMap<S> create(Type typeOfId) {
    return new IdMap<S>(typeOfId);
  }
}
