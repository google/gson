package com.google.gson.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import junit.framework.TestCase;

public class IterableTest extends TestCase {
  private static class CustomIterable implements Iterable<Integer> {
    final int base;

    public CustomIterable(int base) {
      this.base = base;
    }

    @Override
    public Iterator<Integer> iterator() {
      return Arrays.asList(base + 1, base + 2).iterator();
    }
  }

  public void testSerialize() {
    CustomIterable iterable = new CustomIterable(0);

    Gson gson = new Gson();
    // Serializing specific Iterable subtype should use reflection-based approach
    assertEquals("{\"base\":0}", gson.toJson(iterable));
    // But serializing as Iterable should use adapter
    assertEquals("[1,2]", gson.toJson(iterable, Iterable.class));
  }

  public void testDeserialize() {
    Gson gson = new Gson();
    // Deserializing as specific Iterable subtype should use reflection-based approach
    // i.e. must not choose any (potentially incompatible) class implementing `Iterable`
    // See also https://github.com/google/gson/issues/1708
    assertEquals(1, gson.fromJson("{\"base\":1}", CustomIterable.class).base);

    // But deserializing as Iterable should use adapter
    Iterable<Integer> deserialized = gson.fromJson("[1,2]", new TypeToken<Iterable<Integer>>() {}.getType());
    // Collect elements and then compare them to not make any assumptions about
    // type of `deserialized`
    ArrayList<Integer> elements = new ArrayList<Integer>();
    for (Integer element : deserialized) {
      elements.add(element);
    }
    assertEquals(Arrays.asList(1, 2), elements);
  }

  private static class CustomIterableTypeAdapter extends TypeAdapter<Iterable<?>> {
    private static final int SERIALIZED = 5;

    private static List<Integer> getDeserialized() {
      return Arrays.asList(1, 2);
    }

    @Override public void write(JsonWriter out, Iterable<?> value) throws IOException {
      out.value(SERIALIZED);
    }

    @Override public Iterable<?> read(JsonReader in) throws IOException {
      in.skipValue();
      return getDeserialized();
    }
  }

  /**
   * Verify that overwriting built-in {@link Iterable} adapter is possible.
   */
  public void testCustomAdapter() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Iterable.class, new CustomIterableTypeAdapter())
      .create();

    String expectedJson = String.valueOf(CustomIterableTypeAdapter.SERIALIZED);
    assertEquals(expectedJson, gson.toJson(new ArrayList<String>(), Iterable.class));

    List<Integer> expectedDeserialized = CustomIterableTypeAdapter.getDeserialized();
    assertEquals(expectedDeserialized, gson.fromJson("[]", Iterable.class));
  }
}
