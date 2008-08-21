package com.google.gson;

import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Unit tests for {@link TypeUtils}.
 *
 * @author Inderjeet Singh
 *
 */
public class TypeUtilsTest extends TestCase {
  private static final Type MAP_TYPE = new TypeToken<Map<String, Integer>>() {}.getType();

  public void testGetActualTypeForFirstTypeVariable() {
    assertEquals(String.class, TypeUtils.getActualTypeForFirstTypeVariable(MAP_TYPE));
  }

  public void testIsArrayForNonArrayClasses() {
    assertFalse(TypeUtils.isArray(Boolean.class));
    assertFalse(TypeUtils.isArray(MAP_TYPE));
  }

  public void testIsArrayForArrayClasses() {
    assertTrue(TypeUtils.isArray(String[].class));
    assertTrue(TypeUtils.isArray(Integer[][].class));
    assertTrue(TypeUtils.isArray(Collection[].class));
  }

  public void testToRawClassForNonGenericClasses() {
    assertEquals(String.class, TypeUtils.toRawClass(String.class));
  }

  public void testToRawClassForGenericClasses() {
    assertEquals(Map.class, TypeUtils.toRawClass(MAP_TYPE));
  }
}
