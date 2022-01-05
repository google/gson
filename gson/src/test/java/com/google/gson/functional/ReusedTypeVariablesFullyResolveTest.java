package com.google.gson.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This test covers the scenario described in #1390 where a type variable needs to be used
 * by a type definition multiple times.  Both type variable references should resolve to the
 * same underlying concrete type.
 */
class ReusedTypeVariablesFullyResolveTest {

  private Gson gson;

  @BeforeEach
  void setUp() {
    gson = new GsonBuilder().create();
  }

  @SuppressWarnings("ConstantConditions") // The instances were being unmarshaled as Strings instead of TestEnums
  @Test
  void testGenericsPreservation() {
    TestEnumSetCollection withSet = gson.fromJson("{\"collection\":[\"ONE\",\"THREE\"]}", TestEnumSetCollection.class);
    Iterator<TestEnum> iterator = withSet.collection.iterator();
    assertNotNull(withSet);
    assertNotNull(withSet.collection);
    assertEquals(2, withSet.collection.size());
    TestEnum first = iterator.next();
    TestEnum second = iterator.next();

    assertTrue(first instanceof TestEnum);
    assertTrue(second instanceof TestEnum);
  }

  enum TestEnum { ONE, TWO, THREE }

  private static class TestEnumSetCollection extends SetCollection<TestEnum> {}

  private static class SetCollection<T> extends BaseCollection<T, Set<T>> {}

  private static class BaseCollection<U, C extends Collection<U>>
  {
    public C collection;
  }

}
