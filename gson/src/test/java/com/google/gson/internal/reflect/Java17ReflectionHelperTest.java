package com.google.gson.internal.reflect;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.util.Objects;
import org.junit.Test;

public class Java17ReflectionHelperTest {
  @Test
  public void testJava17Record() throws ClassNotFoundException {
    Class<?> unixDomainPrincipalClass = Class.forName("jdk.net.UnixDomainPrincipal");
    // UnixDomainPrincipal is a record
    assertTrue(ReflectionHelper.isRecord(unixDomainPrincipalClass));
    // with 2 components
    assertArrayEquals(
        new String[] {"user", "group"},
        ReflectionHelper.getRecordComponentNames(unixDomainPrincipalClass));
    // Check canonical constructor
    Constructor<?> constructor =
        ReflectionHelper.getCanonicalRecordConstructor(unixDomainPrincipalClass);
    assertNotNull(constructor);
    assertArrayEquals(
        new Class<?>[] {UserPrincipal.class, GroupPrincipal.class},
        constructor.getParameterTypes());
  }

  @Test
  public void testJava17RecordAccessors() throws ReflectiveOperationException {
    // Create an instance of UnixDomainPrincipal, using our custom implementation of UserPrincipal,
    // and GroupPrincipal. Then attempt to access each component of the record using our accessor
    // methods.
    Class<?> unixDomainPrincipalClass = Class.forName("jdk.net.UnixDomainPrincipal");
    Object unixDomainPrincipal =
        ReflectionHelper.getCanonicalRecordConstructor(unixDomainPrincipalClass)
            .newInstance(new PrincipalImpl("user"), new PrincipalImpl("group"));

    String[] componentNames = ReflectionHelper.getRecordComponentNames(unixDomainPrincipalClass);
    assertTrue(componentNames.length > 0);

    for (String componentName : componentNames) {
      Field componentField = unixDomainPrincipalClass.getDeclaredField(componentName);
      Method accessor = ReflectionHelper.getAccessor(unixDomainPrincipalClass, componentField);
      Object principal = accessor.invoke(unixDomainPrincipal);

      assertEquals(new PrincipalImpl(componentName), principal);
    }
  }

  /** Implementation of {@link UserPrincipal} and {@link GroupPrincipal} just for record tests. */
  public static class PrincipalImpl implements UserPrincipal, GroupPrincipal {
    private final String name;

    public PrincipalImpl(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof PrincipalImpl) {
        return Objects.equals(name, ((PrincipalImpl) o).name);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
    }
  }
}
