package com.google.gson.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.ReflectPermission;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Permission;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

public class ReflectionAccessTest {
  @SuppressWarnings("unused")
  private static class ClassWithPrivateMembers {
    private String s;

    private ClassWithPrivateMembers() {
    }
  }

  private static Class<?> loadClassWithDifferentClassLoader(Class<?> c) throws Exception {
    URL url = c.getProtectionDomain().getCodeSource().getLocation();
    URLClassLoader classLoader = new URLClassLoader(new URL[] { url }, null);
    return classLoader.loadClass(c.getName());
  }

  @Test
  public void testRestrictiveSecurityManager() throws Exception {
    // Must use separate class loader, otherwise permission is not checked, see Class.getDeclaredFields()
    Class<?> clazz = loadClassWithDifferentClassLoader(ClassWithPrivateMembers.class);

    final Permission accessDeclaredMembers = new RuntimePermission("accessDeclaredMembers");
    final Permission suppressAccessChecks = new ReflectPermission("suppressAccessChecks");
    SecurityManager original = System.getSecurityManager();
    SecurityManager restrictiveManager = new SecurityManager() {
      @Override
      public void checkPermission(Permission perm) {
        if (accessDeclaredMembers.equals(perm)) {
          throw new SecurityException("Gson: no-member-access");
        }
        if (suppressAccessChecks.equals(perm)) {
          throw new SecurityException("Gson: no-suppress-access-check");
        }
      }
    };
    System.setSecurityManager(restrictiveManager);

    try {
      Gson gson = new Gson();
      try {
        // Getting reflection based adapter should fail
        gson.getAdapter(clazz);
        fail();
      } catch (SecurityException e) {
        assertEquals("Gson: no-member-access", e.getMessage());
      }

      final AtomicBoolean wasReadCalled = new AtomicBoolean(false);
      gson = new GsonBuilder()
        .registerTypeAdapter(clazz, new TypeAdapter<Object>() {
          @Override
          public void write(JsonWriter out, Object value) throws IOException {
            out.value("custom-write");
          }

          @Override
          public Object read(JsonReader in) throws IOException {
            in.skipValue();
            wasReadCalled.set(true);
            return null;
          }}
        )
        .create();

      assertEquals("\"custom-write\"", gson.toJson(null, clazz));
      assertNull(gson.fromJson("{}", clazz));
      assertTrue(wasReadCalled.get());
    } finally {
      System.setSecurityManager(original);
    }
  }

  /**
   * Test serializing an instance of a non-accessible internal class, but where
   * Gson supports serializing one of its superinterfaces.
   *
   * <p>Here {@link Collections#emptyList()} is used which returns an instance
   * of the internal class {@code java.util.Collections.EmptyList}. Gson should
   * serialize the object as {@code List} despite the internal class not being
   * accessible.
   *
   * <p>See https://github.com/google/gson/issues/1875
   */
  @Test
  public void testSerializeInternalImplementationObject() {
    Gson gson = new Gson();
    String json = gson.toJson(Collections.emptyList());
    assertEquals("[]", json);

    // But deserialization should fail
    Class<?> internalClass = Collections.emptyList().getClass();
    try {
      gson.fromJson("{}", internalClass);
      fail("Missing exception; test has to be run with `--illegal-access=deny`");
    } catch (JsonIOException expected) {
      assertTrue(expected.getMessage().startsWith(
          "Failed making constructor 'java.util.Collections$EmptyList#EmptyList()' accessible; "
          + "either change its visibility or write a custom InstanceCreator or TypeAdapter for its declaring type"
      ));
    }
  }
}
