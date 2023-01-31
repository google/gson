/*
 * Copyright (C) 2021 Google Inc.
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

package com.google.gson.internal.reflect;

import com.google.gson.JsonIOException;
import com.google.gson.internal.GsonBuildConfig;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionHelper {

  private static final RecordHelper RECORD_HELPER;

  static {
    RecordHelper instance;
    try {
      // Try to construct the RecordSupportedHelper, if this fails, records are not supported on this JVM.
      instance = new RecordSupportedHelper();
    } catch (NoSuchMethodException e) {
      instance = new RecordNotSupportedHelper();
    }
    RECORD_HELPER = instance;
  }

  private ReflectionHelper() {}

  /**
   * Internal implementation of making an {@link AccessibleObject} accessible.
   *
   * @param object the object that {@link AccessibleObject#setAccessible(boolean)} should be called on.
   * @throws JsonIOException if making the object accessible fails
   */
  public static void makeAccessible(AccessibleObject object) throws JsonIOException {
    try {
      object.setAccessible(true);
    } catch (Exception exception) {
      String description = getAccessibleObjectDescription(object, false);
      throw new JsonIOException("Failed making " + description + " accessible; either increase its visibility"
              + " or write a custom TypeAdapter for its declaring type.", exception);
    }
  }

  /**
   * Returns a short string describing the {@link AccessibleObject} in a human-readable way.
   * The result is normally shorter than {@link AccessibleObject#toString()} because it omits
   * modifiers (e.g. {@code final}) and uses simple names for constructor and method parameter
   * types.
   *
   * @param object object to describe
   * @param uppercaseFirstLetter whether the first letter of the description should be uppercased
   */
  public static String getAccessibleObjectDescription(AccessibleObject object, boolean uppercaseFirstLetter) {
    String description;

    if (object instanceof Field) {
      description = "field '" + fieldToString((Field) object) + "'";
    } else if (object instanceof Method) {
      Method method = (Method) object;

      StringBuilder methodSignatureBuilder = new StringBuilder(method.getName());
      appendExecutableParameters(method, methodSignatureBuilder);
      String methodSignature = methodSignatureBuilder.toString();

      description = "method '" + method.getDeclaringClass().getName() + "#" + methodSignature + "'";
    } else if (object instanceof Constructor) {
      description = "constructor '" + constructorToString((Constructor<?>) object) + "'";
    } else {
      description = "<unknown AccessibleObject> " + object.toString();
    }

    if (uppercaseFirstLetter && Character.isLowerCase(description.charAt(0))) {
      description = Character.toUpperCase(description.charAt(0)) + description.substring(1);
    }
    return description;
  }

  /**
   * Creates a string representation for a field, omitting modifiers and
   * the field type.
   */
  public static String fieldToString(Field field) {
    return field.getDeclaringClass().getName() + "#" + field.getName();
  }

  /**
   * Creates a string representation for a constructor.
   * E.g.: {@code java.lang.String(char[], int, int)}
   */
  public static String constructorToString(Constructor<?> constructor) {
    StringBuilder stringBuilder = new StringBuilder(constructor.getDeclaringClass().getName());
    appendExecutableParameters(constructor, stringBuilder);

    return stringBuilder.toString();
  }

  // Note: Ideally parameter type would be java.lang.reflect.Executable, but that was added in Java 8
  private static void appendExecutableParameters(AccessibleObject executable, StringBuilder stringBuilder) {
    stringBuilder.append('(');

    Class<?>[] parameters = (executable instanceof Method)
        ? ((Method) executable).getParameterTypes()
        : ((Constructor<?>) executable).getParameterTypes();
    for (int i = 0; i < parameters.length; i++) {
      if (i > 0) {
        stringBuilder.append(", ");
      }
      stringBuilder.append(parameters[i].getSimpleName());
    }

    stringBuilder.append(')');
  }

  /**
   * Tries making the constructor accessible, returning an exception message
   * if this fails.
   *
   * @param constructor constructor to make accessible
   * @return exception message; {@code null} if successful, non-{@code null} if
   *    unsuccessful
   */
  public static String tryMakeAccessible(Constructor<?> constructor) {
    try {
      constructor.setAccessible(true);
      return null;
    } catch (Exception exception) {
      return "Failed making constructor '" + constructorToString(constructor) + "' accessible;"
          + " either increase its visibility or write a custom InstanceCreator or TypeAdapter for"
          // Include the message since it might contain more detailed information
          + " its declaring type: " + exception.getMessage();
    }
  }

  /** If records are supported on the JVM, this is equivalent to a call to Class.isRecord() */
  public static boolean isRecord(Class<?> raw) {
    return RECORD_HELPER.isRecord(raw);
  }

  public static String[] getRecordComponentNames(Class<?> raw) {
    return RECORD_HELPER.getRecordComponentNames(raw);
  }

  /** Looks up the record accessor method that corresponds to the given record field */
  public static Method getAccessor(Class<?> raw, Field field) {
    return RECORD_HELPER.getAccessor(raw, field);
  }

  public static <T> Constructor<T> getCanonicalRecordConstructor(Class<T> raw) {
    return RECORD_HELPER.getCanonicalRecordConstructor(raw);
  }

  public static RuntimeException createExceptionForUnexpectedIllegalAccess(
      IllegalAccessException exception) {
    throw new RuntimeException("Unexpected IllegalAccessException occurred (Gson " + GsonBuildConfig.VERSION + ")."
        + " Certain ReflectionAccessFilter features require Java >= 9 to work correctly. If you are not using"
        + " ReflectionAccessFilter, report this to the Gson maintainers.",
        exception);
  }


  private static RuntimeException createExceptionForRecordReflectionException(
          ReflectiveOperationException exception) {
    throw new RuntimeException("Unexpected ReflectiveOperationException occurred"
            + " (Gson " + GsonBuildConfig.VERSION + ")."
            + " To support Java records, reflection is utilized to read out information"
            + " about records. All these invocations happens after it is established"
            + " that records exist in the JVM. This exception is unexpected behavior.",
            exception);
  }

  /**
   * Internal abstraction over reflection when Records are supported.
   */
  private abstract static class RecordHelper {
    abstract boolean isRecord(Class<?> clazz);

    abstract String[] getRecordComponentNames(Class<?> clazz);

    abstract <T> Constructor<T> getCanonicalRecordConstructor(Class<T> raw);

    public abstract Method getAccessor(Class<?> raw, Field field);
  }

  private static class RecordSupportedHelper extends RecordHelper {
    private final Method isRecord;
    private final Method getRecordComponents;
    private final Method getName;
    private final Method getType;

    private RecordSupportedHelper() throws NoSuchMethodException {
      isRecord = Class.class.getMethod("isRecord");
      getRecordComponents = Class.class.getMethod("getRecordComponents");
      // Class java.lang.reflect.RecordComponent
      Class<?> classRecordComponent = getRecordComponents.getReturnType().getComponentType();
      getName = classRecordComponent.getMethod("getName");
      getType = classRecordComponent.getMethod("getType");
    }

    @Override
    boolean isRecord(Class<?> raw) {
      try {
        return (boolean) isRecord.invoke(raw);
      } catch (ReflectiveOperationException e) {
        throw createExceptionForRecordReflectionException(e);
      }
    }

    @Override
    String[] getRecordComponentNames(Class<?> raw) {
      try {
        Object[] recordComponents = (Object[]) getRecordComponents.invoke(raw);
        String[] componentNames = new String[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i++) {
          componentNames[i] = (String) getName.invoke(recordComponents[i]);
        }
        return componentNames;
      } catch (ReflectiveOperationException e) {
        throw createExceptionForRecordReflectionException(e);
      }
    }

    @Override
    public <T> Constructor<T> getCanonicalRecordConstructor(Class<T> raw) {
      try {
        Object[] recordComponents = (Object[]) getRecordComponents.invoke(raw);
        Class<?>[] recordComponentTypes = new Class<?>[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i++) {
          recordComponentTypes[i] = (Class<?>) getType.invoke(recordComponents[i]);
        }
        // Uses getDeclaredConstructor because implicit constructor has same visibility as record and might
        // therefore not be public
        return raw.getDeclaredConstructor(recordComponentTypes);
      } catch (ReflectiveOperationException e) {
        throw createExceptionForRecordReflectionException(e);
      }
    }

    @Override
    public Method getAccessor(Class<?> raw, Field field) {
      try {
        // Records consists of record components, each with a unique name, a corresponding field and accessor method
        // with the same name. Ref.: https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html#jls-8.10.3
        return raw.getMethod(field.getName());
      } catch (ReflectiveOperationException e) {
        throw createExceptionForRecordReflectionException(e);
      }
    }
  }

  /**
   * Instance used when records are not supported
   */
  private static class RecordNotSupportedHelper extends RecordHelper {

    @Override
    boolean isRecord(Class<?> clazz) {
      return false;
    }

    @Override
    String[] getRecordComponentNames(Class<?> clazz) {
      throw new UnsupportedOperationException(
              "Records are not supported on this JVM, this method should not be called");
    }

    @Override
    <T> Constructor<T> getCanonicalRecordConstructor(Class<T> raw) {
      throw new UnsupportedOperationException(
              "Records are not supported on this JVM, this method should not be called");
    }

    @Override
    public Method getAccessor(Class<?> raw, Field field) {
      throw new UnsupportedOperationException(
              "Records are not supported on this JVM, this method should not be called");
    }
  }
}
