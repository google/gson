package com.google.gson.functional;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * This test covers the scenario described in #1390 where a type variable needs to be used
 * by a type definition multiple times.  Both type variable references should resolve to the
 * same underlying concrete type.
 */
public class ReusedTypeVariablesFullyResolveTest {

  private Gson gson;

  @Before
  public void setUp() {
    gson = new GsonBuilder().create();
  }

  @SuppressWarnings("ConstantConditions") // The instances were being unmarshaled as Strings instead of TestEnums
  @Test
  public void testGenericsPreservation() {
    TestEnumSetCollection withSet = gson.fromJson("{\"collection\":[\"ONE\",\"THREE\"]}", TestEnumSetCollection.class);
    Iterator<TestEnum> iterator = withSet.collection.iterator();
    assertThat(withSet).isNotNull();
    assertThat(withSet.collection).isNotNull();
    assertThat(withSet.collection).hasSize(2);
    TestEnum first = iterator.next();
    TestEnum second = iterator.next();

    assertThat(first).isInstanceOf(TestEnum.class);
    assertThat(second).isInstanceOf(TestEnum.class);
  }

  enum TestEnum { ONE, TWO, THREE }

  private static class TestEnumSetCollection extends SetCollection<TestEnum> {}

  private static class SetCollection<T> extends BaseCollection<T, Set<T>> {}

  private static class BaseCollection<U, C extends Collection<U>>
  {
    public C collection;
  }

}
