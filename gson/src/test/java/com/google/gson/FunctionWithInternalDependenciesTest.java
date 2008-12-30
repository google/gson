package com.google.gson;

import com.google.gson.common.TestTypes.ClassWithNoFields;

import junit.framework.TestCase;

import java.lang.reflect.Modifier;

/**
 * Functional tests for Gson that depend on some internal package-protected elements of
 * com.google.gson package and hence must be placed in the same package. We should make every
 * attempt to migrate tests out of this class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class FunctionWithInternalDependenciesTest extends TestCase {

  public void testAnonymousLocalClassesSerialization() {
    Gson gson = new Gson(new ModifierBasedExclusionStrategy(
        true, Modifier.TRANSIENT, Modifier.STATIC), Gson.DEFAULT_NAMING_POLICY);
    assertEquals("{}", gson.toJson(new ClassWithNoFields() {
      // empty anonymous class
    }));
  }
}
