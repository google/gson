/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.typeadapters;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class BaseRuntimeTypeAdapterFactory<T,S> implements TypeAdapterFactory {
    private final Class<T> baseType;
    private final String typeFieldName;
    private Class<S> labelType;
    private final TypeToLabelConverter<T,S> converter;
    private final Map<S, Class<?>> labelToSubtype = new LinkedHashMap<S, Class<?>>();
    private final Map<Class<?>, S> subtypeToLabel = new LinkedHashMap<Class<?>, S>();
    private boolean useBaseType;

    protected BaseRuntimeTypeAdapterFactory(Class<T> baseType, String typeFieldName, Class<S> labelType, boolean useBaseType, TypeToLabelConverter<T,S> aConverter) {
        if (typeFieldName == null || baseType == null) {
            throw new NullPointerException();
        }
        this.baseType = baseType;
        this.typeFieldName = typeFieldName;
        this.converter = aConverter;
        this.useBaseType = useBaseType;
        this.labelType = labelType;
    }

    public interface TypeToLabelConverter<T,S> {
        S Transform(Class<? extends T> aType);
    }

    /**
     * Creates a new runtime type adapter using for {@code baseType} using {@code
     * typeFieldName} as the type field name. Type field names are case sensitive.
     */
    public static <T,S> BaseRuntimeTypeAdapterFactory<T,S> of(Class<T> baseType, String typeFieldName, Class<S> labelType, boolean useBaseType, TypeToLabelConverter<T,S> aConverter) {
        return new BaseRuntimeTypeAdapterFactory<T,S>(baseType, typeFieldName, labelType, useBaseType, aConverter);
    }

    /**
     * Creates a new runtime type adapter for {@code baseType} using {@code "type"} as
     * the type field name.
     */
    public static <T,S> BaseRuntimeTypeAdapterFactory<T,S> of(Class<T> baseType, Class<S> labelType, boolean useBaseType, TypeToLabelConverter<T,S> aConverter) {
        return new BaseRuntimeTypeAdapterFactory<T,S>(baseType, "type", labelType, useBaseType, aConverter);
    }

    /**
     * Registers {@code type} identified by {@code label}. Labels are case
     * sensitive.
     *
     * @throws IllegalArgumentException if either {@code type} or {@code label}
     *     have already been registered on this type adapter.
     */
    public BaseRuntimeTypeAdapterFactory<T,S> registerSubtype(Class<? extends T> type, S label) {
        if (type == null || label == null) {
            throw new NullPointerException();
        }
        if (subtypeToLabel.containsKey(type) || labelToSubtype.containsKey(label)) {
            throw new IllegalArgumentException("types and labels must be unique");
        }
        labelToSubtype.put(label, type);
        subtypeToLabel.put(type, label);

        return this;
    }

    /**
     * Registers {@code type} identified by its {@link Class#getSimpleName simple
     * name}. Labels are case sensitive.
     *
     * @throws IllegalArgumentException if either {@code type} or its simple name
     *     have already been registered on this type adapter.
     */
    public BaseRuntimeTypeAdapterFactory<T,S> registerSubtype(Class<? extends T> type) {
        if (converter == null){
            throw new NullPointerException();
        }

        return registerSubtype(type, converter.Transform(type));
    }

    public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
        if (!baseType.isAssignableFrom(type.getRawType())) {
            return null;
        }

        if (labelToSubtype.size() == 0) {
            throw new JsonParseException("No sub-types have been registered for " + this);
        }

        final TypeAdapter<S> labelTypeAdapter = gson.getAdapter(labelType);
        final Map<S, TypeAdapter<?>> labelToDelegate = new LinkedHashMap<S, TypeAdapter<?>>();
        final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate = new LinkedHashMap<Class<?>, TypeAdapter<?>>();

        for (Map.Entry<S, Class<?>> entry : labelToSubtype.entrySet()) {
            TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
            labelToDelegate.put(entry.getKey(), delegate);
            subtypeToDelegate.put(entry.getValue(), delegate);
        }

        return new TypeAdapter<R>() {
            @Override public R read(JsonReader in) throws IOException {
                return fromJsonTree(Streams.parse(in));
            }

            @Override public R fromJsonTree(JsonElement jsonTree) {
                TypeAdapter<R> delegate;
                S label = null;

                JsonElement labelJsonElement = jsonTree.getAsJsonObject().remove(typeFieldName);
                if (labelJsonElement == null)
                {
                    if (useBaseType) {
                        delegate = (TypeAdapter<R>) subtypeToDelegate.get(baseType);
                    } else {
                        throw new JsonParseException("cannot deserialize " + baseType
                                + " because it does not define a field named " + typeFieldName);
                    }
                }
                else
                {
                    label = labelTypeAdapter.fromJsonTree(labelJsonElement);
                    // @SuppressWarnings("unchecked") // registration requires that subtype extends T
                    delegate = (TypeAdapter<R>) labelToDelegate.get(label);
                }

                if (delegate == null) {
                    throw new JsonParseException("cannot deserialize " + baseType + " subtype named "
                            + label + "; did you forget to register a subtype?");
                }
                return delegate.fromJsonTree(jsonTree);
            }

            @Override public void write(JsonWriter out, R value) throws IOException {
                Streams.write(toJsonTree(value), out);
            }

            @Override public JsonElement toJsonTree(R value) {
                Class<?> srcType = value.getClass();
                S label = subtypeToLabel.get(srcType);
                @SuppressWarnings("unchecked") // registration requires that subtype extends T
                        TypeAdapter<R> delegate = (TypeAdapter<R>) subtypeToDelegate.get(srcType);
                if (delegate == null) {
                    throw new JsonParseException("cannot serialize " + srcType.getName()
                            + "; did you forget to register a subtype?");
                }
                JsonElement element = delegate.toJsonTree(value);
                if (!element.isJsonObject()){
                    throw new JsonParseException(element + " is not a json object");
                }

                JsonObject jsonObject = element.getAsJsonObject();
                if (jsonObject.has(typeFieldName)) {
                    throw new JsonParseException("cannot serialize " + srcType.getName()
                            + " because it already defines a field named " + typeFieldName);
                }
                JsonObject clone = new JsonObject();
                clone.add(typeFieldName, labelTypeAdapter.toJsonTree(label));
                for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                    clone.add(e.getKey(), e.getValue());
                }

                return clone;
            }
        }.nullSafe();
    }
}
