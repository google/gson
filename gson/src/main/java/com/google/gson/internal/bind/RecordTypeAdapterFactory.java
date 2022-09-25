package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * {@link RecordFieldFactory} to create adapters for Java 17 records.
 *
 * <p>This class makes the following assumptions about records:
 *
 * <ul>
 *   <li>>The name in the RecordComponent corresponds to a field in the class
 *   <li>>The order of RecordComponents is the same as for the Record canonical constructor
 *   <li>>When the isRecord method is present on the Class, RecordComponent will also exist in the
 *       same JVM.
 * </ul>
 */
public class RecordTypeAdapterFactory implements TypeAdapterFactory {

  public static final boolean SUPPORTS_RECORD_TYPES;

  private static final RecordHelper RECORD_HELPER;

  static {
    Method isRecord;
    try {
      isRecord = Class.class.getDeclaredMethod("isRecord");
    } catch (NoSuchMethodException e) {
      // If the isRecord is not defined, then we assume we are not on Java 17 or later, and there is
      // no record support.
      isRecord = null;
    }
    RECORD_HELPER = (isRecord == null) ? null : new RecordHelper(isRecord);
    SUPPORTS_RECORD_TYPES = RECORD_HELPER != null;
  }

  private final Excluder excluder;
  private final ConstructorConstructor constructorConstructor;
  private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory;

  public RecordTypeAdapterFactory(
      Excluder excluder,
      ConstructorConstructor constructorConstructor,
      JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory) {
    this.excluder = excluder;
    this.constructorConstructor = constructorConstructor;
    this.jsonAdapterFactory = jsonAdapterFactory;
  }

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    // ?: Is this not a record?
    if (!RECORD_HELPER.isRecord(type)) {
      // Yes -> Not supported, we return null as per the TypeAdapterFactory contract
      return null;
    }

    try {
      // To construct record fields, we actually need to do some reflection on the Gson instance.
      // This
      // logic is contained in the RecordFieldFactory.
      RecordFieldFactory recordFieldFactory =
          new RecordFieldFactory(gson, excluder, jsonAdapterFactory, constructorConstructor);

      Object[] recordComponents = RECORD_HELPER.getRecordComponents(type);
      Class<?>[] componentTypes = new Class<?>[recordComponents.length];
      for (int i = 0; i < recordComponents.length; i++) {
        componentTypes[i] = RECORD_HELPER.getType(recordComponents[i]);
      }

      // Find the canonical constructor on the Record that corresponds to the record components.
      // There is no method in the Java API to do the equivalent, instead we rely on the constructor
      // and recordComponent order to be the same. This construct matches the StackOverflow answer
      // here: https://stackoverflow.com/a/67127067
      @SuppressWarnings("unchecked")
      Constructor<T> recordConstructor =
          (Constructor<T>) type.getRawType().getConstructor(componentTypes);

      RecordField[] recordFields = new RecordField[recordComponents.length];
      for (int i = 0; i < recordComponents.length; i++) {
        // We need the field for the Gson Excluder, so that we can correctly determine if a field
        // should be included or not.
        Field field = RECORD_HELPER.getField(recordComponents[i], type);
        recordFields[i] = recordFieldFactory.createRecordField(field, recordComponents[i]);
      }
      return new RecordTypeAdapterImpl<>(recordFields, recordConstructor);
    } catch (NoSuchMethodException e) {
      // We hit this either because we do not find the expected constructor, or we do not find a
      // method name that matches the field name. In either of these classes, this is not a record,
      // and we do not support serialization of it. The contract for TypeAdapterFactory is to return
      // null for unsupported types. Gson will then attempt other means of creating a TypeAdapter.
      // If all fails, an exception will be created where Gson was invoked to serialize/deserialize
      // a type.
      return null;
    }
  }

  /**
   * Internal helper class to manage access to Class.isRecord, and RecordComponent instances. Since
   * this compiles on Java 8, we need to use reflection to handle RecordComponent instances.
   */
  private static final class RecordHelper {
    private final Method isRecord;
    private final Method getRecordComponents;
    // RecordComponent methods
    private final Method getName;
    private final Method getType;
    private final Method getGenericType;
    // getAccessor returns a method, that in turn can be invoked on the Record to read out its value
    private final Method getAccessor;
    private final Method getAnnotation;

    /** Create a new RecordHelper to handle reflection for RecordComponent on Java 17. */
    private RecordHelper(Method isRecord) {
      this.isRecord = Objects.requireNonNull(isRecord, "isRecord must not be null");
      try {
        getRecordComponents = Class.class.getDeclaredMethod("getRecordComponents");
        Class<?> recordComponentType = getRecordComponents.getReturnType().getComponentType();
        getName = recordComponentType.getDeclaredMethod("getName");
        getType = recordComponentType.getDeclaredMethod("getType");
        getGenericType = recordComponentType.getDeclaredMethod("getGenericType");
        getAccessor = recordComponentType.getDeclaredMethod("getAccessor");
        getAnnotation = recordComponentType.getDeclaredMethod("getAnnotation", Class.class);

      } catch (NoSuchMethodException e) {
        throw new TypeAdapterReflectionException(
            "Expected to find method getRecordComponents when the isRecord method is present in"
                + " Class",
            e);
      }
    }

    boolean isRecord(TypeToken<?> type) {
      try {
        return isRecord != null && Boolean.TRUE.equals(isRecord.invoke(type));
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new TypeAdapterReflectionException(
            "Unable to create TypeAdapter for [" + type + "]", e);
      }
    }

    Object[] getRecordComponents(TypeToken<?> type) {
      try {
        return (Object[]) getRecordComponents.invoke(type.getRawType());
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new TypeAdapterReflectionException("Unable to invoke getRecordComponents", e);
      }
    }

    String getName(Object recordComponent) {
      try {
        return (String) getName.invoke(recordComponent);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new TypeAdapterReflectionException(
            "Failed to invoke method [getName] on RecordComponent", e);
      }
    }

    Class<?> getType(Object recordComponent) {
      try {
        return (Class<?>) getType.invoke(recordComponent);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new TypeAdapterReflectionException(
            "Failed to invoke method [getGenericType] on RecordComponent", e);
      }
    }

    Type getGenericType(Object recordComponent) {
      try {
        return (Type) getGenericType.invoke(recordComponent);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new TypeAdapterReflectionException(
            "Failed to invoke method [getGenericType] on RecordComponent", e);
      }
    }
    // getAccessor returns a method, that in turn can be invoked on the Record to read out its value
    Method getAccessor(Object recordComponent) {
      try {
        return (Method) getAccessor.invoke(recordComponent);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new TypeAdapterReflectionException(
            "Failed to invoke method [getAccessor] on RecordComponent", e);
      }
    }

    <A extends Annotation> A getAnnotation(Object recordComponent, Class<A> annotation) {
      try {
        return annotation.cast(getAnnotation.invoke(recordComponent, annotation));
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new TypeAdapterReflectionException(
            "Failed to invoke method [getAnnotation] on RecordComponent", e);
      }
    }

    public <T> Field getField(Object recordComponent, TypeToken<T> type) {
      // There is nothing in the RecordComponent class to access the underlying field, so we do this
      // based
      // on the component name.
      String fieldName = getName(recordComponent);
      try {
        return type.getRawType().getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        throw new TypeAdapterReflectionException(
            "Expected to find field ["
                + fieldName
                + "] on recordComponent ["
                + recordComponent
                + "]"
                + " on type ["
                + type
                + "]. Somehow there is a discrepancy between the record components"
                + " and the fields on the Class.",
            e);
      }
    }
  }

  /**
   * Factory to construct our own {@link RecordField} instances. This requires some reflection into
   * the internal of Gson, which has logic to handle the {@link JsonAdapter} annotation. We also
   * need to reflect into Gson to fetch the {@link Excluder}, that handles the {@link
   * com.google.gson.annotations.Expose}, {@link com.google.gson.annotations.Since} and {@link
   * com.google.gson.annotations.Until} annotations. This allows us to closely follow the Gson
   * mechanisms wrt. serialization/deserialization.
   */
  private static final class RecordFieldFactory {

    private final Gson gson;
    private final Excluder excluder;
    private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory;
    private final ConstructorConstructor constructorConstructor;

    public RecordFieldFactory(
        Gson gson,
        Excluder excluder,
        JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory,
        ConstructorConstructor constructorConstructor) {
      this.gson = gson;
      this.excluder = excluder;
      this.jsonAdapterFactory = jsonAdapterFactory;
      this.constructorConstructor = constructorConstructor;
    }

    private RecordField createRecordField(Field field, Object recordComponent) {
      TypeToken<?> fieldType = TypeToken.get(RECORD_HELPER.getGenericType(recordComponent));
      JsonAdapter jsonAdapter = RECORD_HELPER.getAnnotation(recordComponent, JsonAdapter.class);
      TypeAdapter<?> typeAdapter;
      if (jsonAdapter == null) {
        typeAdapter = gson.getAdapter(fieldType);
      } else {
        typeAdapter =
            jsonAdapterFactory.getTypeAdapter(constructorConstructor, gson, fieldType, jsonAdapter);
      }

      // Determine the serialized and deserialized names. If there is an SerializedName annotation
      // present, we need to respect it. This might also mean that multiple keys acts as aliases
      // when reading Json.
      SerializedName serializedNameAnnotation =
          RECORD_HELPER.getAnnotation(recordComponent, SerializedName.class);
      String serializedName;
      String[] deserializedNames;
      if (serializedNameAnnotation == null) {
        serializedName = RECORD_HELPER.getName(recordComponent);
        deserializedNames = new String[] {RECORD_HELPER.getName(recordComponent)};
      } else {
        serializedName = serializedNameAnnotation.value();
        deserializedNames = new String[serializedNameAnnotation.alternate().length + 1];
        deserializedNames[0] = serializedName;
        System.arraycopy(
            serializedNameAnnotation.alternate(),
            0,
            // Start at offset 1, as the default value is stored at index 0
            deserializedNames,
            1,
            serializedNameAnnotation.alternate().length);
      }

      return new RecordField(
          gson.serializeNulls(),
          excluder.excludeField(field, true),
          excluder.excludeField(field, false),
          RECORD_HELPER.getAccessor(recordComponent),
          serializedName,
          deserializedNames,
          typeAdapter);
    }
  }

  /** Helper class to contain all the rules for a single field, in regard to Gson annotations. */
  private static final class RecordField {
    // Configured on Gson itself, if null values should be included in output
    private final boolean serializeNulls;
    // Configured via Expose / Since / Until annotations
    private final boolean excludeOnSerialize;
    private final boolean excludeOnDeSerialize;
    // The public record method for reading out values from an instance of the record.
    private final Method accessor;
    // The name to write when serializing this field on a record
    private final String serializedName;
    // Which names we expect to find when deserializing this record
    private final String[] deSerializedNames;
    // Adapter for reading/write the json version of a record field.
    @SuppressWarnings("rawtypes")
    private final TypeAdapter typeAdapter;

    private final Object defaultValue;

    private RecordField(
        boolean serializeNulls,
        boolean excludeOnSerialize,
        boolean excludeOnDeSerialize,
        Method accessor,
        String serializedName,
        String[] deSerializedNames,
        TypeAdapter<?> typeAdapter) {
      this.serializeNulls = serializeNulls;
      this.excludeOnSerialize = excludeOnSerialize;
      this.excludeOnDeSerialize = excludeOnDeSerialize;
      this.accessor = accessor;
      this.serializedName = serializedName;
      this.deSerializedNames = deSerializedNames;
      this.typeAdapter = typeAdapter;
      if (accessor.getReturnType().isPrimitive()) {
        // To initialize primitives, we use reflection to create an array of size 1, and get the
        // first element.
        defaultValue = Array.get(Array.newInstance(accessor.getReturnType(), 1), 0);
      } else {
        defaultValue = null;
      }
    }

    @SuppressWarnings("unchecked")
    private void appendNameAndValue(JsonWriter out, Object value) throws IOException {
      try {
        Object fieldValue = accessor.invoke(value);
        // Respect the Gson config with regard to nulls.
        if (!excludeOnSerialize && fieldValue != null || serializeNulls) {
          out.name(serializedName);
          typeAdapter.write(out, fieldValue);
        }
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(
            "Failed to serialize field [" + accessor + "] on [" + value + "]", e);
      }
    }

    private Object readValueForKey(JsonReader in) throws IOException {
      if (excludeOnDeSerialize) {
        // Since we are always in an object context, this is a safe way to skip values
        // we do not wish to deserialize.
        in.skipValue();
        return null;
      } else {
        return typeAdapter.read(in);
      }
    }

    @Override
    public String toString() {
      String classSimpleName = accessor.getDeclaringClass().getSimpleName();
      return "RecordField<"
          + classSimpleName
          + "."
          + accessor.getName()
          + " to "
          + serializedName
          + ">";
    }
  }

  private static final class RecordTypeAdapterImpl<T> extends TypeAdapter<T> {
    // Each field in the record, as managed by a RecordField instance.
    private final RecordField[] recordFields;
    // This map holds the index in the above array for each name we expect to find on
    // de-serialization. This
    // is used to organize the deserialized values into an Object[] that has the same order as the
    // constructor arguments.
    private final Map<String, Integer> recordFieldIndex = new HashMap<>();
    // The actual record constructor.
    private final Constructor<T> constructor;
    private final Object[] constuctorDefaultValues;

    private RecordTypeAdapterImpl(RecordField[] recordFields, Constructor<T> constructor) {
      this.recordFields = recordFields;
      this.constructor = constructor;
      for (int i = 0; i < recordFields.length; i++) {
        RecordField recordField = recordFields[i];
        for (String name : recordField.deSerializedNames) {
          Integer prevIndex = recordFieldIndex.put(name, i);
          if (prevIndex != null) {
            throw new IllegalArgumentException(
                "Both ["
                    + recordFields[prevIndex]
                    + "]"
                    + " and ["
                    + recordField
                    + "] can be read from the same name ["
                    + name
                    + "]");
          }
        }
      }
      constuctorDefaultValues = new Object[recordFields.length];
      for (int i = 0; i < recordFields.length; i++) {
        constuctorDefaultValues[i] = recordFields[i].defaultValue;
      }
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      out.beginObject();
      for (RecordField recordField : recordFields) {
        recordField.appendNameAndValue(out, value);
      }
      out.endObject();
    }

    @Override
    public T read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      } else if (in.peek() != JsonToken.BEGIN_OBJECT) {
        throw new DeserializeException("Expecting null or begin object at [" + in.getPath() + "]");
      }
      in.beginObject();
      // This array will hold the value for each json field in the src object that we find,
      // deserialized using a RecordField, that will also respect all Gson annotations. For fields
      // that are not deserialized, the default null value will be used.
      Object[] values = new Object[recordFields.length];
      // Copy the default values, this will ensure that primitives are non-null.
      System.arraycopy(constuctorDefaultValues, 0, values, 0, values.length);

      while (in.peek() != JsonToken.END_OBJECT) {
        String fieldName = in.nextName();
        // Check if we know which field this name should deserialize to. If we do not find a
        // matching index, then this field is one that is not present in our record, and ignored. We
        // do not treat unknown object fields as an error, because the version of the class we have
        // might differ from the source.
        Integer fieldIndex = recordFieldIndex.get(fieldName);
        if (fieldIndex != null) {
          values[fieldIndex] = recordFields[fieldIndex].readValueForKey(in);
        } else {
          // Be sure to skip values we do not consume, otherwise the state of the parser will be
          // wrong.
          in.skipValue();
        }
      }
      in.endObject();
      // At this point, we have not verified that all fields are present, we treat absent values as
      // null before passing on to the constructor. We could verify that all fields are present, but
      // the src could have different rules about serializing null values than we do. So it is
      // better to ignore that we are missing some values, rather than throw here. -stun.
      try {
        return constructor.newInstance(values);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw new DeserializeAccessException(
            "Failed to create an instance of ["
                + constructor.getDeclaringClass()
                + "] at ["
                + in.getPath()
                + "]",
            e);
      }
    }
  }

  // ========= Exceptions =========================================================================

  public static class TypeAdapterReflectionException extends RuntimeException {

    private static final long serialVersionUID = -3905048094470359103L;

    public TypeAdapterReflectionException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static class DeserializeAccessException extends RuntimeException {
    private static final long serialVersionUID = 5565314742287167598L;

    public DeserializeAccessException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static class DeserializeException extends RuntimeException {

    private static final long serialVersionUID = 7554437800495514736L;

    public DeserializeException(String message) {
      super(message);
    }
  }
}
