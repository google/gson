package com.google.gson.internal;

import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.TestStringMapGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.Feature;
import com.google.common.collect.testing.features.MapFeature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

/**
 * Dynamic {@link MapTestSuiteBuilder Map test suite} for {@link LinkedTreeMap}. This complements
 * {@link LinkedTreeMapTest}.
 */
@RunWith(AllTests.class)
public class LinkedTreeMapSuiteTest {
  private static class MapGenerator extends TestStringMapGenerator {
    private final boolean allowNullValues;

    public MapGenerator(boolean allowNullValues) {
      this.allowNullValues = allowNullValues;
    }

    @Override
    protected Map<String, String> create(Entry<String, String>[] entries) {
      var map = new LinkedTreeMap<String, String>(allowNullValues);
      for (var entry : entries) {
        map.put(entry.getKey(), entry.getValue());
      }
      return map;
    }
  }

  private static Feature<?>[] createFeatures(Feature<?>... additionalFeatures) {
    // Don't specify CollectionFeature.SERIALIZABLE because Guava testlib seems to assume
    // deserialized Map has same properties as original map (e.g. disallowing null keys), but this
    // is not the case for LinkedTreeMap which is serialized as JDK LinkedHashMap
    var features =
        new ArrayList<Feature<?>>(
            List.of(
                CollectionSize.ANY,
                // Note: There is current a Guava bug which causes 'null additions' to not be tested
                // if 'null queries' is enabled, see https://github.com/google/guava/issues/7401
                MapFeature.ALLOWS_ANY_NULL_QUERIES,
                MapFeature.RESTRICTS_KEYS, // Map only allows comparable keys
                MapFeature.SUPPORTS_PUT,
                MapFeature.SUPPORTS_REMOVE,
                // Affects keySet, values and entrySet (?)
                CollectionFeature.KNOWN_ORDER, // Map preserves insertion order
                CollectionFeature.SUPPORTS_ITERATOR_REMOVE));
    features.addAll(Arrays.asList(additionalFeatures));
    return features.toArray(Feature[]::new);
  }

  // Special method recognized by JUnit's `AllTests` runner
  public static Test suite() {
    var nullValuesSuite =
        MapTestSuiteBuilder.using(new MapGenerator(true))
            .withFeatures(createFeatures(MapFeature.ALLOWS_NULL_VALUES))
            .named("nullValues=true")
            .createTestSuite();

    var nonNullValuesSuite =
        MapTestSuiteBuilder.using(new MapGenerator(false))
            .withFeatures(createFeatures())
            .named("nullValues=false")
            .createTestSuite();

    TestSuite testSuite = new TestSuite("LinkedTreeMap");
    testSuite.addTest(nullValuesSuite);
    testSuite.addTest(nonNullValuesSuite);

    return testSuite;
  }
}
