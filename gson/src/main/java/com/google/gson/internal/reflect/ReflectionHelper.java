package com.google.gson.internal.reflect;

import com.google.gson.JsonIOException;
import com.google.gson.internal.GsonBuildConfig;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    try {
      field.setAccessible(true);
    } catch (Exception exception) {
      throw new JsonIOException("Failed making field '" + field.getDeclaringClass().getName() + "#"
          + field.getName() + "' accessible; either change its visibility or write a custom "
          + "TypeAdapter for its declaring type", exception);
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

  public static boolean isRecord(Class<?> raw) {
    return RECORD_HELPER.isRecord(raw);
  }

  public static String[] recordFields(Class<?> raw) {
    return RECORD_HELPER.recordFields(raw);
  }

  public static RuntimeException createExceptionForUnexpectedIllegalAccess(
      IllegalAccessException exception) {
    throw new RuntimeException("Unexpected IllegalAccessException occurred (Gson " + GsonBuildConfig.VERSION + "). "
        + "Certain ReflectionAccessFilter features require Java >= 9 to work correctly. If you are not using "
        + "ReflectionAccessFilter, report this to the Gson maintainers.",
        exception);
  }

  public static <T> Constructor<T> getCanonicalRecordConstructor(Class<T> raw) {
    return RECORD_HELPER.getCanonicalRecordConstructor(raw);
  }

  /**
   * Internal abstraction over reflection when Records are supported.
   */
  private abstract static class RecordHelper {
    abstract boolean isRecord(Class<?> clazz);

    abstract String[] recordFields(Class<?> clazz);

    abstract <T> Constructor<T> getCanonicalRecordConstructor(Class<T> raw);
  }

  private static class RecordSupportedHelper extends RecordHelper {
    private final Method isRecord;
    private final Method getRecordComponents;
    private final Method getName;
    private final Method getType;

    private RecordSupportedHelper() throws NoSuchMethodException {
      isRecord = Class.class.getMethod("isRecord");
      getRecordComponents = Class.class.getDeclaredMethod("getRecordComponents");
      Class<?> recordComponentType = getRecordComponents.getReturnType().getComponentType();
      getName = recordComponentType.getDeclaredMethod("getName");
      getType = recordComponentType.getDeclaredMethod("getType");
    }

    @Override
    boolean isRecord(Class<?> raw) {
      try {
        return boolean.class.cast(isRecord.invoke(raw));
      } catch (IllegalAccessException e) {
        throw createExceptionForUnexpectedIllegalAccess(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException("Failed to reflect into record components on"
                + " class [" + raw + "], this is unexpected behaviour, as these methods should"
                + " not throw when they are defined on Class",
                e
        );
      }
    }

    @Override
    String[] recordFields(Class<?> raw) {
      try {
        Object recordComponents = getRecordComponents.invoke(raw);
        int componentCount = Array.getLength(recordComponents);
        String[] recordFields = new String[componentCount];
        for (int i = 0; i < componentCount; i++) {
          recordFields[i] =
                  String.class.cast(getName.invoke(Array.get(recordComponents, i)));
        }
        return recordFields;
      } catch (IllegalAccessException e) {
        throw createExceptionForUnexpectedIllegalAccess(e);
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException("Failed to reflect into record components on"
                + " class [" + raw + "], this is unexpected behaviour, as these methods should"
                + " not throw when they are defined on Class",
                e
        );
      }
    }

    @Override
    public <T> Constructor<T> getCanonicalRecordConstructor(Class<T> raw) {
      try {
        Object recordComponents = getRecordComponents.invoke(raw);
        int componentCount = Array.getLength(recordComponents);
        Class[] recordFields = new Class[componentCount];
        for (int i = 0; i < componentCount; i++) {
          recordFields[i] =
                  Class.class.cast(getType.invoke(Array.get(recordComponents, i)));
        }
        return raw.getConstructor(recordFields);
      } catch (IllegalAccessException e) {
        throw createExceptionForUnexpectedIllegalAccess(e);
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException("Failed to reflect into record components on"
                + " class [" + raw + "], this is unexpected behaviour, as these methods should"
                + " not throw when they are defined on Class",
                e
        );
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
    String[] recordFields(Class<?> clazz) {
      throw new UnsupportedOperationException(
              "Records are not supported on this JVM, this method should not be called");
    }

    @Override
    <T> Constructor<T> getCanonicalRecordConstructor(Class<T> raw) {
      throw new UnsupportedOperationException(
              "Records are not supported on this JVM, this method should not be called");
    }
  }
}
