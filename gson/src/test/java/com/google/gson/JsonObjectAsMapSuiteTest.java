package com.google.gson;

import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.SampleElements;
import com.google.common.collect.testing.TestMapGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

/**
 * Dynamic {@link MapTestSuiteBuilder Map test suite} for {@link JsonObject#asMap()}. This
 * complements {@link JsonObjectAsMapTest}, which can cover some cases which are not covered here,
 * e.g. making sure changes in the {@code Map} view are visible in the {@code JsonObject}.
 */
@RunWith(AllTests.class)
public class JsonObjectAsMapSuiteTest {
  private static class MapGenerator implements TestMapGenerator<String, JsonElement> {
    @Override
    public SampleElements<Entry<String, JsonElement>> samples() {
      return new SampleElements<>(
          Map.entry("one", JsonNull.INSTANCE),
          Map.entry("two", new JsonPrimitive(true)),
          Map.entry("three", new JsonPrimitive("test")),
          Map.entry("four", new JsonArray()),
          Map.entry("five", new JsonObject()));
    }

    @Override
    public Map<String, JsonElement> create(Object... elements) {
      JsonObject object = new JsonObject();
      for (Object element : elements) {
        var entry = (Entry<?, ?>) element;
        object.add((String) entry.getKey(), (JsonElement) entry.getValue());
      }
      return object.asMap();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Entry<String, JsonElement>[] createArray(int length) {
      return (Entry<String, JsonElement>[]) new Entry<?, ?>[length];
    }

    @Override
    public Iterable<Entry<String, JsonElement>> order(
        List<Entry<String, JsonElement>> insertionOrder) {
      // Preserves insertion order
      return insertionOrder;
    }

    @Override
    public String[] createKeyArray(int length) {
      return new String[length];
    }

    @Override
    public JsonElement[] createValueArray(int length) {
      return new JsonElement[length];
    }
  }

  // Special method recognized by JUnit's `AllTests` runner
  public static Test suite() {
    return MapTestSuiteBuilder.using(new MapGenerator())
        .withFeatures(
            CollectionSize.ANY,
            // Note: There is current a Guava bug which causes 'null additions' to not be tested if
            // 'null queries' is enabled, see https://github.com/google/guava/issues/7401
            MapFeature.ALLOWS_ANY_NULL_QUERIES,
            MapFeature.RESTRICTS_KEYS, // Map only allows String keys
            MapFeature.RESTRICTS_VALUES, // Map only allows JsonElement values
            MapFeature.SUPPORTS_PUT,
            MapFeature.SUPPORTS_REMOVE,
            // Affects keySet, values and entrySet (?)
            CollectionFeature.KNOWN_ORDER, // Map preserves insertion order
            CollectionFeature.SUPPORTS_ITERATOR_REMOVE)
        .named("JsonObject#asMap")
        .createTestSuite();
  }
}
