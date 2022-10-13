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
   * Tries making the field accessible, wrapping any thrown exception in a {@link JsonIOException}
   * with descriptive message.
   *
   * @param field field to make accessible
   * @throws JsonIOException if making the field accessible fails
   */
  public static void makeAccessible(Field field) throws JsonIOException {
    makeAccessible("field '" + field.getDeclaringClass().getName() + "#" + field.getName() + "'", field);
  }

  /**
   * Tries making the constructor accessible, wrapping any thrown exception in a {@link JsonIOException}
   * with descriptive message.
   *
   * @param constructor constructor to make accessible
   * @throws JsonIOException if making the constructor accessible fails
   */
  public static void makeAccessible(Constructor<?> constructor) throws JsonIOException {
    makeAccessible(
            "constructor " + constructor + " in " + constructor.getDeclaringClass().getName(),
            constructor
    );
  }

  /**
   * Internal implementation of making an {@link AccessibleObject} accessible.
   *
   * @param description describe what we are attempting to make accessible
   * @param object the object that {@link AccessibleObject#setAccessible(boolean)} should be called on.
   * @throws JsonIOException if making the object accessible fails
   */
  private static void makeAccessible(String description, AccessibleObject object) throws JsonIOException {
    try {
      object.setAccessible(true);
    } catch (Exception exception) {
      throw new JsonIOException("Failed making " + description + "' accessible; either change its visibility "
              + "or write a custom TypeAdapter for its declaring type", exception);
    }
  }

  /**
   * Creates a string representation for a constructor.
   * E.g.: {@code java.lang.String#String(char[], int, int)}
   */
  private static String constructorToString(Constructor<?> constructor) {
    StringBuilder stringBuilder = new StringBuilder(constructor.getDeclaringClass().getName())
      .append('#')
      .append(constructor.getDeclaringClass().getSimpleName())
      .append('(');
    Class<?>[] parameters = constructor.getParameterTypes();
    for (int i = 0; i < parameters.length; i++) {
      if (i > 0) {
        stringBuilder.append(", ");
      }
      stringBuilder.append(parameters[i].getSimpleName());
    }

    return stringBuilder.append(')').toString();
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
      return "Failed making constructor '" + constructorToString(constructor) + "' accessible; "
          + "either change its visibility or write a custom InstanceCreator or TypeAdapter for its declaring type: "
          // Include the message since it might contain more detailed information
          + exception.getMessage();
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
    throw new RuntimeException("Unexpected IllegalAccessException occurred (Gson " + GsonBuildConfig.VERSION + "). "
        + "Certain ReflectionAccessFilter features require Java >= 9 to work correctly. If you are not using "
        + "ReflectionAccessFilter, report this to the Gson maintainers.",
        exception);
  }


  public static RuntimeException createExceptionForRecordReflectionException(
          ReflectiveOperationException exception) {
    throw new RuntimeException("Unexpected ReflectiveOperationException occurred "
            + "(Gson " + GsonBuildConfig.VERSION + "). "
            + "To support Java records, reflection is utilized to read out information "
            + "about records. All these invocations happens after it is established "
            + "that records exists in the JVM. This exception is unexpected behaviour.",
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
      Class<?> recordComponentType = getRecordComponents.getReturnType().getComponentType();
      getName = recordComponentType.getMethod("getName");
      getType = recordComponentType.getMethod("getType");
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
