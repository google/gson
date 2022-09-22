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

package com.google.gson.internal.bind;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.ReflectionAccessFilter;
import com.google.gson.ReflectionAccessFilter.FilterResult;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.Primitives;
import com.google.gson.internal.ReflectionAccessFilterHelper;
import com.google.gson.internal.reflect.ReflectionHelper;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Type adapter that reflects over the fields and methods of a class.
 */
public final class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {
  private final ConstructorConstructor constructorConstructor;
  private final FieldNamingStrategy fieldNamingPolicy;
  private final Excluder excluder;
  private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory;
  private final List<ReflectionAccessFilter> reflectionFilters;

  public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor,
      FieldNamingStrategy fieldNamingPolicy, Excluder excluder,
      JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory,
      List<ReflectionAccessFilter> reflectionFilters) {
    this.constructorConstructor = constructorConstructor;
    this.fieldNamingPolicy = fieldNamingPolicy;
    this.excluder = excluder;
    this.jsonAdapterFactory = jsonAdapterFactory;
    this.reflectionFilters = reflectionFilters;
  }

  private boolean includeField(Field f, boolean serialize) {
    return !excluder.excludeClass(f.getType(), serialize) && !excluder.excludeField(f, serialize);
  }

  /** first element holds the default name */
  private List<String> getFieldNames(Field f) {
    SerializedName annotation = f.getAnnotation(SerializedName.class);
    if (annotation == null) {
      String name = fieldNamingPolicy.translateName(f);
      return Collections.singletonList(name);
    }

    String serializedName = annotation.value();
    String[] alternates = annotation.alternate();
    if (alternates.length == 0) {
      return Collections.singletonList(serializedName);
    }

    List<String> fieldNames = new ArrayList<>(alternates.length + 1);
    fieldNames.add(serializedName);
    for (String alternate : alternates) {
      fieldNames.add(alternate);
    }
    return fieldNames;
  }

  @Override
  public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {
    Class<? super T> raw = type.getRawType();

    if (!Object.class.isAssignableFrom(raw)) {
      return null; // it's a primitive!
    }

    if (ReflectionHelper.isRecord(raw)) {
      // We can assume blockInaccessible here, because it will never kick inn. We are not doing
      // direct field access, and are instead constructing records via their canonical constructor.
      return new RecordAdapter<>(raw, getBoundFields(gson, type, raw, true));
    }

    FilterResult filterResult =
        ReflectionAccessFilterHelper.getFilterResult(reflectionFilters, raw);
    if (filterResult == FilterResult.BLOCK_ALL) {
      throw new JsonIOException(
          "ReflectionAccessFilter does not permit using reflection for "
              + raw
              + ". Register a TypeAdapter for this type or adjust the access filter.");
    }
    boolean blockInaccessible = filterResult == FilterResult.BLOCK_INACCESSIBLE;

    ObjectConstructor<T> constructor = constructorConstructor.get(type);
    return new FieldReflectionAdapter<>(constructor, getBoundFields(gson, type, raw, blockInaccessible));
  }

  private static void checkAccessible(Object object, Field field) {
    if (!ReflectionAccessFilterHelper.canAccess(field, Modifier.isStatic(field.getModifiers()) ? null : object)) {
      throw new JsonIOException("Field '" + field.getDeclaringClass().getName() + "#"
          + field.getName() + "' is not accessible and ReflectionAccessFilter does not "
          + "permit making it accessible. Register a TypeAdapter for the declaring type "
          + "or adjust the access filter.");
    }
  }

  private ReflectiveTypeAdapterFactory.BoundField createBoundField(
      final Gson context, final Field field, final String name,
      final TypeToken<?> fieldType, boolean serialize, boolean deserialize,
      final boolean blockInaccessible) {
    final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
    JsonAdapter annotation = field.getAnnotation(JsonAdapter.class);
    TypeAdapter<?> mapped = null;
    if (annotation != null) {
      // This is not safe; requires that user has specified correct adapter class for @JsonAdapter
      mapped = jsonAdapterFactory.getTypeAdapter(
          constructorConstructor, context, fieldType, annotation);
    }
    final boolean jsonAdapterPresent = mapped != null;
    if (mapped == null) mapped = context.getAdapter(fieldType);

    @SuppressWarnings("unchecked")
    final TypeAdapter<Object> typeAdapter = (TypeAdapter<Object>) mapped;
    return new ReflectiveTypeAdapterFactory.BoundField(name, serialize, deserialize) {
      @Override void write(JsonWriter writer, Object value)
          throws IOException, IllegalAccessException {
        if (!serialized) return;
        if (blockInaccessible) {
          checkAccessible(value, field);
        }

        Object fieldValue = field.get(value);
        if (fieldValue == value) {
          // avoid direct recursion
          return;
        }
        writer.name(name);
        TypeAdapter<Object> t = jsonAdapterPresent ? typeAdapter
            : new TypeAdapterRuntimeTypeWrapper<>(context, typeAdapter, fieldType.getType());
        t.write(writer, fieldValue);
      }

      @Override
      Object read(JsonReader reader) throws IOException {
        return typeAdapter.read(reader);
      }

      @Override
      void read(JsonReader reader, Object value)
          throws IOException, IllegalAccessException {
        Object fieldValue = typeAdapter.read(reader);
        if (fieldValue != null || !isPrimitive) {
          if (blockInaccessible) {
            checkAccessible(value, field);
          }
          field.set(value, fieldValue);
        }
      }
    };
  }

  private Map<String, BoundField> getBoundFields(Gson context, TypeToken<?> type, Class<?> raw, boolean blockInaccessible) {
    Map<String, BoundField> result = new LinkedHashMap<>();
    if (raw.isInterface()) {
      return result;
    }

    Type declaredType = type.getType();
    Class<?> originalRaw = raw;
    while (raw != Object.class) {
      Field[] fields = raw.getDeclaredFields();

      // For inherited fields, check if access to their declaring class is allowed
      if (raw != originalRaw && fields.length > 0) {
        FilterResult filterResult = ReflectionAccessFilterHelper.getFilterResult(reflectionFilters, raw);
        if (filterResult == FilterResult.BLOCK_ALL) {
          throw new JsonIOException("ReflectionAccessFilter does not permit using reflection for "
              + raw + " (supertype of " + originalRaw + "). Register a TypeAdapter for this type "
              + "or adjust the access filter.");
        }
        blockInaccessible = filterResult == FilterResult.BLOCK_INACCESSIBLE;
      }

      for (Field field : fields) {
        boolean serialize = includeField(field, true);
        boolean deserialize = includeField(field, false);
        if (!serialize && !deserialize) {
          continue;
        }

        // If blockInaccessible, skip and perform access check later
        if (!blockInaccessible) {
          ReflectionHelper.makeAccessible(field);
        }
        Type fieldType = $Gson$Types.resolve(type.getType(), raw, field.getGenericType());
        List<String> fieldNames = getFieldNames(field);
        BoundField previous = null;
        for (int i = 0, size = fieldNames.size(); i < size; ++i) {
          String name = fieldNames.get(i);
          if (i != 0) serialize = false; // only serialize the default name
          BoundField boundField = createBoundField(context, field, name,
              TypeToken.get(fieldType), serialize, deserialize, blockInaccessible);
          BoundField replaced = result.put(name, boundField);
          if (previous == null) previous = replaced;
        }
        if (previous != null) {
          throw new IllegalArgumentException(declaredType
              + " declares multiple JSON fields named " + previous.name);
        }
      }
      type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
      raw = type.getRawType();
    }
    return result;
  }

  static abstract class BoundField {
    final String name;
    final boolean serialized;
    final boolean deserialized;

    protected BoundField(String name, boolean serialized, boolean deserialized) {
      this.name = name;
      this.serialized = serialized;
      this.deserialized = deserialized;
    }

    abstract void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException;

    abstract Object read(JsonReader reader) throws IOException;

    abstract void read(JsonReader reader, Object value) throws IOException, IllegalAccessException;
  }

  /**
   * Base class for Adapters produced by this factory.
   *
   * <p>The {@link RecordAdapter} is a special case to handle Java 17 and later records, for all
   * other types we use the {@link FieldReflectionAdapter}. This class encapuslates the common logic
   * for serialization and deserialization. During deserialization, we construct an intermediary
   * ResultHolder R, which we append values to. After the object has been read in full, the {@link
   * #finalize(Object)} method is used to convert the intermediary to an instance of T.
   *
   * @param <T> type of objects that this Adapter creates.
   * @param <I> intermediary type used to build the deserialization result.
   */
  abstract static class Adapter<T, I> extends TypeAdapter<T> {
    protected final Map<String, BoundField> boundFields;

    protected Adapter(Map<String, BoundField> boundFields) {
      this.boundFields = boundFields;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }

      out.beginObject();
      try {
        for (BoundField boundField : boundFields.values()) {
          boundField.write(out, value);
        }
      } catch (IllegalAccessException e) {
        throw ReflectionHelper.createExceptionForUnexpectedIllegalAccess(e);
      }
      out.endObject();
    }

    @Override
    public T read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      I intermediary = createIntermediary();

      try {
        in.beginObject();
        while (in.hasNext()) {
          String name = in.nextName();
          BoundField field = boundFields.get(name);
          if (field == null || !field.deserialized) {
            in.skipValue();
          } else {
            readField(intermediary, in, field);
          }
        }
      } catch (IllegalStateException e) {
        throw new JsonSyntaxException(e);
      } catch (IllegalAccessException e) {
        throw ReflectionHelper.createExceptionForUnexpectedIllegalAccess(e);
      }
      in.endObject();
      return finalize(intermediary);
    }

    // Create the Object that will be used to collect each field value
    abstract I createIntermediary();
    // Read a single Bounded field into the intermediary. The JsonReader will be pointed at the
    // start of the value
    // for the BoundField to read from.
    abstract void readField(I intermediary, JsonReader in, BoundField field)
        throws IllegalAccessException, IOException;
    // Convert the intermediary to a final instance of T.
    abstract T finalize(I intermediary);
  }

  private static final class FieldReflectionAdapter<T> extends Adapter<T, T> {
    private final ObjectConstructor<T> constructor;

    FieldReflectionAdapter(ObjectConstructor<T> constructor, Map<String, BoundField> boundFields) {
      super(boundFields);
      this.constructor = constructor;
    }

    @Override
    T createIntermediary() {
      return constructor.construct();
    }

    @Override
    void readField(T intermediary, JsonReader in, BoundField field)
        throws IllegalAccessException, IOException {
      field.read(in, intermediary);
    }

    @Override
    T finalize(T intermediary) {
      return intermediary;
    }
  }

  private static final class RecordAdapter<T> extends Adapter<T, Object[]> {
    // The actual record constructor.
    private final Constructor<? super T> constructor;
    // Array of arguments to the constructor, initialized with default values for primitives
    private final Object[] constructorArgsDefaults;
    // Map from field names to index into the constructors arguments.
    private final Map<String, Integer> fieldIndices = new HashMap<>();

    RecordAdapter(Class<? super T> raw, Map<String, BoundField> boundFields) {
      super(boundFields);
      this.constructor = ReflectionHelper.getCanonicalRecordConstructor(raw);
      String[] recordFields = ReflectionHelper.recordFields(raw);
      for (int i = 0; i < recordFields.length; i++) {
        fieldIndices.put(recordFields[i], i);
      }
      Class<?>[] parameterTypes = constructor.getParameterTypes();
      constructorArgsDefaults = new Object[parameterTypes.length];
      for (int i = 0; i < parameterTypes.length; i++) {
        if (parameterTypes[i].isPrimitive()) {
          constructorArgsDefaults[i] = Array.get(Array.newInstance(parameterTypes[i], 1), 0);
        }
      }
    }

    @Override
    Object[] createIntermediary() {
      Object[] constructorArgs = new Object[constructorArgsDefaults.length];
      System.arraycopy(constructorArgsDefaults, 0, constructorArgs, 0, constructorArgs.length);
      return constructorArgs;
    }

    @Override
    void readField(Object[] intermediary, JsonReader in, BoundField field) throws IOException {
      Integer fieldIndex = fieldIndices.get(field.name);
      if (fieldIndex == null) {
        throw new IllegalStateException(
            "Could not find the index in the constructor "
                + constructor
                + " for field with name "
                + field.name
                + ", unable to determine which argument in the constructor the field corresponds"
                + " to. This is unexpected behaviour, as we expect the RecordComponents to have the"
                + " same names as the fields in the Java class, and that the order of the"
                + " RecordComponents is the same as the order of the canonical arguments.");
      }
      intermediary[fieldIndex] = field.read(in);
    }

    @Override
    @SuppressWarnings("unchecked")
    T finalize(Object[] intermediary) {
      try {
        return (T) constructor.newInstance(intermediary);
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(
            "Failed to invoke " + constructor + " with args " + Arrays.toString(intermediary), e);
      }
    }
  }
}
