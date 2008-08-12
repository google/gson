package com.google.gson;

import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Small test for the {@link ArrayTypeInfo}.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ArrayTypeInfoTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testArray() {
    String[] a = {"a", "b", "c"};
    ArrayTypeInfo typeInfo = new ArrayTypeInfo(a.getClass());
    assertEquals(a.getClass(), typeInfo.getRawType());
    assertEquals(String.class, typeInfo.getComponentRawType());
  }

  public void testArrayOfArrays() {
    String[][] a = {
        new String[]{"a1", "a2", "a3"},
        new String[]{"b1", "b2", "b3"},
        new String[]{"c1", "c2", "c3"}};
    ArrayTypeInfo typeInfo = new ArrayTypeInfo(a.getClass());
    assertEquals(a.getClass(), typeInfo.getRawType());
    assertEquals(String.class, typeInfo.getComponentRawType());
    assertEquals(String[].class, typeInfo.getSecondLevelClass());
  }

  public void testParameterizedArray() {
    Type type = new TypeToken<List<String>[]>() {}.getType();
    ArrayTypeInfo typeInfo = new ArrayTypeInfo(type);
    assertEquals(List[].class, typeInfo.getRawType());
    assertEquals(List.class, typeInfo.getComponentRawType());
  }

  public void testParameterizedArrayOfArrays() {
    Type type = new TypeToken<List<String>[][]>() {}.getType();
    ArrayTypeInfo typeInfo = new ArrayTypeInfo(type);
    assertEquals(List[][].class, typeInfo.getRawType());
    assertEquals(List[].class, typeInfo.getSecondLevelClass());
    assertEquals(List.class, typeInfo.getComponentRawType());
  }

  public void testNestedParameterizedArray() {
    Type type = new TypeToken<List<List<String>>[]>() {}.getType();
    ArrayTypeInfo typeInfo = new ArrayTypeInfo(type);
    assertEquals(List[].class, typeInfo.getRawType());
    assertEquals(List.class, typeInfo.getComponentRawType());
    assertEquals(List.class, typeInfo.getSecondLevelClass());
  }
}
