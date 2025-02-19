package com.google.gson;

import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.SampleElements;
import com.google.common.collect.testing.TestListGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;
import java.util.List;
import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

/**
 * Dynamic {@link ListTestSuiteBuilder List test suite} for {@link JsonArray#asList()}. This
 * complements {@link JsonArrayAsListTest}, which can cover some cases which are not covered here,
 * e.g. making sure changes in the {@code List} view are visible in the {@code JsonArray}.
 */
@RunWith(AllTests.class)
public class JsonArrayAsListSuiteTest {
  private static class ListGenerator implements TestListGenerator<JsonElement> {
    @Override
    public SampleElements<JsonElement> samples() {
      return new SampleElements<JsonElement>(
          JsonNull.INSTANCE,
          new JsonPrimitive(true),
          new JsonPrimitive("test"),
          new JsonArray(),
          new JsonObject());
    }

    @Override
    public JsonElement[] createArray(int length) {
      return new JsonElement[length];
    }

    @Override
    public Iterable<JsonElement> order(List<JsonElement> insertionOrder) {
      return insertionOrder;
    }

    @Override
    public List<JsonElement> create(Object... elements) {
      JsonArray array = new JsonArray();
      // This is not completely accurate: Because there is no way to directly construct JsonArray or
      // its List view with existing elements, this has to add the elements individually with
      // `List#add`
      var list = array.asList();
      for (Object element : elements) {
        list.add((JsonElement) element);
      }
      return list;
    }
  }

  // Special method recognized by JUnit's `AllTests` runner
  public static Test suite() {
    return ListTestSuiteBuilder.using(new ListGenerator())
        .withFeatures(
            CollectionSize.ANY,
            CollectionFeature.ALLOWS_NULL_QUERIES,
            CollectionFeature.RESTRICTS_ELEMENTS, // List only allows JsonElement
            CollectionFeature.SUPPORTS_ADD,
            ListFeature.REMOVE_OPERATIONS,
            ListFeature.SUPPORTS_ADD_WITH_INDEX,
            ListFeature.SUPPORTS_SET)
        .named("JsonArray#asList")
        .createTestSuite();
  }
}
