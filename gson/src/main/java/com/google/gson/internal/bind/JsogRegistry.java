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

package com.google.gson.internal.bind;

import com.google.gson.GsonLocal;
import com.google.gson.JsogPolicy;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * This helper class acts as a registry for JSOG instances during serialization and deserialization.
 *
 * <p>You should use this in any {@link com.google.gson.TypeAdapter} subclass that supports JSOG</p>
 *
 * @see JsogPolicy
 * @see <a href="https://github.com/jsog/jsog">JSOG</a>
 * @author Paulo Costa
 */
public class JsogRegistry {
    /** Field name where JSOG instance id is stored */
    public static final String JSOG_FIELD_ID = "@id";

    /** Field name where JSOG reference id is stored */
    public static final String JSOG_FIELD_REF = "@ref";

    /**
     * A {@link GsonLocal} is used to bind a different instance to
     * each serialization / deserialization
     */
    private static GsonLocal<JsogRegistry> currentRegistry = new GsonLocal<JsogRegistry>() {
        @Override
        protected JsogRegistry initialValue() {
            return new JsogRegistry();
        }
    };

    /** JSOG policy associated with the current {@link com.google.gson.Gson instance */
    private JsogPolicy jsogPolicy = GsonLocal.gson().getJsogPolicy();
    /** Maps ids to instances */
    private Map<String, Object> idToInstance = new HashMap<String, Object>();
    /** Mapps instances to ids */
    private Map<Object, String> instanceToId = new IdentityHashMap<Object, String>();
    /** The next JSOG id that should be assigned to each prefix */
    private Map<String, Integer> nextIdByPrefix = new HashMap<String, Integer>();

    /** Cannot get an instance directly, only via {@link #get()} */
    private JsogRegistry() {}

    /**
     * Returns the JSOG registry associated with the current serialization / deserialization.
     */
    public static JsogRegistry get() {
        return currentRegistry.get();
    }

    /**
     * Returns the JSOG id associated with this instance, or null
     * if the instance has not been registered
     */
    public String geId(Object instance) {
        return instanceToId.get(instance);
    }

    /**
     * Returns the instance associated with the given JSOG id, or null
     * if the id has not been registered
     */
    public Object getInstance(String id) {
        return idToInstance.get(id);
    }

    /**
     * Register an instance with an auto-generated id
     *
     * @param instance Instance being registered
     * @return The JSOG id associated with the instance
     * @throws IllegalStateException If the object has already been registered
     * @see JsogPolicy#getIdPrefix(Object)
     */
    public String register(Object instance) {
        String prefix = jsogPolicy.getIdPrefix(instance);
        if (prefix == null) {
            prefix = "";
        } else {
            prefix += "-";
        }

        Integer id = nextIdByPrefix.get(prefix);
        if (id == null) {
            id = 1;
        }

        // Ensure automatic IDs don't conflict with manually-registered IDs
        while (idToInstance.containsKey(prefix + id)) {
            id++;
        }
        String fullId = prefix + id;
        register(fullId, instance);
        nextIdByPrefix.put(prefix, id + 1);
        return fullId;
    }

    /**
     * Register an instance with an auto-generated JSOG id
     *
     * @param id JSOG id of the instance
     * @param instance Instance being registered
     * @throws IllegalStateException If the object or the id have already been registered
     */
    public void register(String id, Object instance) {
        if (id == null) {
            throw new NullPointerException("null id");
        }
        if (instance == null) {
            throw new NullPointerException("null instance");
        }
        if (idToInstance.containsKey(id)) {
            throw new IllegalStateException("JsogEnabled id already registered: " + id);
        }
        if (instanceToId.containsKey(instance)) {
            throw new IllegalStateException("JsogEnabled instance already registered: " + instance);
        }
        idToInstance.put(id, instance);
        instanceToId.put(instance, id);
    }
}
