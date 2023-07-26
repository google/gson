package com.google.gson.functional;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.StandardSubjectBuilder;
import com.google.common.truth.Subject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.junit.Test;

/**
 * Tests interoperability with https://github.com/stleary/JSON-java ({@code org.json} package).
 */
public class JsonOrgInteropTest {
  @Test
  public void testNoCustomAdapter() {
    Gson gson = new Gson();
    String expectedMessageArray = "Unsupported class from other JSON library: org.json.JSONArray"
        + "\nSee https://github.com/google/gson/blob/main/Troubleshooting.md#unsupported-json-library-class";
    String expectedMessageObject = "Unsupported class from other JSON library: org.json.JSONObject"
        + "\nSee https://github.com/google/gson/blob/main/Troubleshooting.md#unsupported-json-library-class";

    // TODO: Adjust these once more specific exception type than RuntimeException is thrown
    Exception e = assertThrows(RuntimeException.class, () -> gson.toJson(new JSONArray()));
    assertThat(e).hasMessageThat().isEqualTo(expectedMessageArray);

    e = assertThrows(RuntimeException.class, () -> gson.toJson(new JSONObject()));
    assertThat(e).hasMessageThat().isEqualTo(expectedMessageObject);

    e = assertThrows(RuntimeException.class, () -> gson.fromJson("[]", JSONArray.class));
    assertThat(e).hasMessageThat().isEqualTo(expectedMessageArray);

    e = assertThrows(RuntimeException.class, () -> gson.fromJson("{}", JSONObject.class));
    assertThat(e).hasMessageThat().isEqualTo(expectedMessageObject);
  }

  // Custom classes for equality assertions to avoid using directly JSONArray and JSONObject
  // which perform element wrapping and conversion, and because their `toList()` and `toMap()`
  // methods also recursively convert values
  private static class ExpectedJSONArray {
    public final List<Object> elements;

    public ExpectedJSONArray(List<Object> elements) {
      this.elements = elements;
    }

    @Override
    public String toString() {
      return "JSONArray" + elements;
    }
  }

  private static class ExpectedJSONObject {
    public final Map<String, Object> entries;

    public ExpectedJSONObject(Map<String, Object> entries) {
      this.entries = entries;
    }

    @Override
    public String toString() {
      return "JSONObject" + entries;
    }
  }

  private abstract static class JsonOrgBaseSubject extends Subject {

    protected JsonOrgBaseSubject(FailureMetadata metadata, @Nullable Object actual) {
      super(metadata, actual);
    }

    protected void checkElementValues(String message, Object expected, Object actual) {
      StandardSubjectBuilder builder = check(message);

      if (actual instanceof JSONArray) {
        builder.about(JSONArraySubject.jsonArrays()).that((JSONArray) actual).isEqualTo(expected);
      } else if (actual instanceof JSONObject) {
        builder.about(JSONObjectSubject.jsonObjects()).that((JSONObject) actual).isEqualTo(expected);
      } else {
        builder.that(actual).isEqualTo(expected);
      }
    }
  }

  private static class JSONArraySubject extends JsonOrgBaseSubject {
    private final @Nullable JSONArray actual;

    private JSONArraySubject(FailureMetadata failureMetadata, @Nullable JSONArray subject) {
      super(failureMetadata, subject);
      this.actual = subject;
    }

    public static Factory<JSONArraySubject, JSONArray> jsonArrays() {
      return JSONArraySubject::new;
    }

    public static JSONArraySubject assertThat(@Nullable JSONArray actual) {
      return assertAbout(JSONArraySubject.jsonArrays()).that(actual);
    }

    @Override
    public void isEqualTo(Object expected) {
      if (!(expected instanceof ExpectedJSONArray)) {
        failWithActual("did not expect to be", "a JSONArray");
      }
      isNotNull();

      List<Object> expectedElements = ((ExpectedJSONArray) expected).elements;
      check("length()").that(actual.length()).isEqualTo(expectedElements.size());

      for (int i = 0; i < expectedElements.size(); i++) {
        Object actualElement = actual.opt(i);
        Object expectedElement = expectedElements.get(i);

        checkElementValues("elements[" + i + "]", expectedElement, actualElement);
      }
    }
  }

  private static class JSONObjectSubject extends JsonOrgBaseSubject {
    private final @Nullable JSONObject actual;

    private JSONObjectSubject(FailureMetadata failureMetadata, @Nullable JSONObject subject) {
      super(failureMetadata, subject);
      this.actual = subject;
    }

    public static Factory<JSONObjectSubject, JSONObject> jsonObjects() {
      return JSONObjectSubject::new;
    }

    public static JSONObjectSubject assertThat(@Nullable JSONObject actual) {
      return assertAbout(JSONObjectSubject.jsonObjects()).that(actual);
    }

    @Override
    public void isEqualTo(Object expected) {
      if (!(expected instanceof ExpectedJSONObject)) {
        failWithActual("did not expect to be", "a JSONObject");
      }
      isNotNull();

      Map<String, Object> expectedEntries = ((ExpectedJSONObject) expected).entries;
      check("length()").that(actual.length()).isEqualTo(expectedEntries.size());

      for (Entry<String, Object> expectedEntry : expectedEntries.entrySet()) {
        String expectedKey = expectedEntry.getKey();
        Object actualValue = actual.opt(expectedKey);

        checkElementValues("entries[" + expectedKey + "]", expectedEntry.getValue(), actualValue);
      }
    }
  }



  private static class CustomClass {
    @SuppressWarnings("unused")
    int i = 1;

    @Override
    public String toString() {
      return "custom-toString";
    }
  }

  private static class CustomJsonStringClass implements JSONString {
    @Override
    public String toJSONString() {
      return "\"custom\"";
    }
  }

  // Important: Make sure this class is in-sync with the code in Troubleshooting.md
  /**
   * {@code TypeAdapterFactory} for {@link JSONArray} and {@link JSONObject}.
   *
   * <p>This factory is mainly intended for applications which cannot switch to
   * Gson's own {@link JsonArray} and {@link JsonObject} classes.
   */
  private static class JsonOrgAdapterFactory implements TypeAdapterFactory {
    private abstract static class JsonOrgAdapter<T> extends TypeAdapter<T> {
      private final TypeAdapter<JsonElement> jsonElementAdapter;

      public JsonOrgAdapter(TypeAdapter<JsonElement> jsonElementAdapter) {
        this.jsonElementAdapter = jsonElementAdapter;
      }

      protected abstract T readJsonOrgValue(String json) throws JSONException;

      @Override
      public T read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
          in.nextNull();
          return null;
        }

        // For correctness convert JSON data to string, then let JSON-java parse it;
        // this is pretty inefficient, but makes sure it gets all the corner cases
        // of JSON-java correct
        // However, unlike JSONObject this will not prevent duplicate member names
        JsonElement jsonElement = jsonElementAdapter.read(in);
        String json = jsonElementAdapter.toJson(jsonElement);
        try {
          return readJsonOrgValue(json);
        }
        // For Android this is a checked exception; for the latest JSON-java artifacts it isn't anymore
        catch (JSONException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
          out.nullValue();
          return;
        }

        // For correctness let JSON-java perform JSON conversion, then parse again and write
        // with Gson; this is pretty inefficient, but makes sure it gets all the corner cases
        // of JSON-java correct
        String json = value.toString();
        JsonElement jsonElement = jsonElementAdapter.fromJson(json);
        jsonElementAdapter.write(out, jsonElement);
      }
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      Class<?> rawType = type.getRawType();
      if (rawType != JSONArray.class && rawType != JSONObject.class) {
        return null;
      }

      TypeAdapter<JsonElement> jsonElementAdapter = gson.getAdapter(JsonElement.class);

      TypeAdapter<?> adapter;
      if (rawType == JSONArray.class) {
        adapter = new JsonOrgAdapter<JSONArray>(jsonElementAdapter) {
          @Override
          protected JSONArray readJsonOrgValue(String json) throws JSONException {
            return new JSONArray(json);
          }
        };
      } else {
        adapter = new JsonOrgAdapter<JSONObject>(jsonElementAdapter) {
          @Override
          protected JSONObject readJsonOrgValue(String json) throws JSONException {
            return new JSONObject(json);
          }
        };
      }

      // Safe due to type check at beginning of method
      @SuppressWarnings("unchecked")
      TypeAdapter<T> t = (TypeAdapter<T>) adapter;
      return t;
    }
  }

  /**
   * Tests usage of custom adapters for {@link JSONArray} and {@link JSONObject}.
   *
   * <p>This test also verifies that the code shown in {@code Troubleshooting.md} works
   * as expected.
   */
  @Test
  public void testCustomAdapters() throws JSONException {
    Gson gson = new GsonBuilder()
        .serializeNulls()
        .registerTypeAdapterFactory(new JsonOrgAdapterFactory())
        .create();

    JSONArray array = new JSONArray(Arrays.asList(
        null,
        JSONObject.NULL,
        new CustomClass(),
        new CustomJsonStringClass(),
        123.4,
        true,
        new JSONObject(Collections.singletonMap("key", 1)),
        new JSONArray(Arrays.asList(2)),
        Collections.singletonMap("key", 3),
        Arrays.asList(4),
        new boolean[] {false}
    ));
    assertThat(gson.toJson(array)).isEqualTo(
        "[null,null,\"custom-toString\",\"custom\",123.4,true,{\"key\":1},[2],{\"key\":3},[4],[false]]");
    assertThat(gson.toJson(null, JSONArray.class)).isEqualTo("null");

    JSONObject object = new JSONObject();
    object.put("1", JSONObject.NULL);
    object.put("2", new CustomClass());
    object.put("3", new CustomJsonStringClass());
    object.put("4", 123.4);
    object.put("5", true);
    object.put("6", new JSONObject(Collections.singletonMap("key", 1)));
    object.put("7", new JSONArray(Arrays.asList(2)));
    object.put("8", Collections.singletonMap("key", 3));
    object.put("9", Arrays.asList(4));
    object.put("10", new boolean[] {false});
    assertThat(gson.toJson(object)).isEqualTo(
        "{\"1\":null,\"2\":\"custom-toString\",\"3\":\"custom\",\"4\":123.4,\"5\":true,\"6\":{\"key\":1},\"7\":[2],\"8\":{\"key\":3},\"9\":[4],\"10\":[false]}");
    assertThat(gson.toJson(null, JSONObject.class)).isEqualTo("null");

    ExpectedJSONArray expectedArray = new ExpectedJSONArray(Arrays.asList(
        JSONObject.NULL,
        true,
        12,
        "string",
        new ExpectedJSONObject(Collections.singletonMap("key", 1)),
        new ExpectedJSONArray(Arrays.asList(2))
    ));
    String json = "[null, true, 12, \"string\", {\"key\": 1}, [2]]";
    JSONArraySubject.assertThat(gson.fromJson(json, JSONArray.class)).isEqualTo(expectedArray);
    assertThat(gson.fromJson("null", JSONArray.class)).isNull();

    Map<String, Object> expectedObject = new HashMap<>();
    expectedObject.put("1", JSONObject.NULL);
    expectedObject.put("2", true);
    expectedObject.put("3", 12);
    expectedObject.put("4", "string");
    expectedObject.put("5", new ExpectedJSONObject(Collections.singletonMap("key", 1)));
    expectedObject.put("6", new ExpectedJSONArray(Arrays.asList(2)));
    json = "{\"1\": null, \"2\": true, \"3\": 12, \"4\": \"string\", \"5\": {\"key\": 1}, \"6\": [2]}";
    JSONObjectSubject.assertThat(gson.fromJson(json, JSONObject.class)).isEqualTo(new ExpectedJSONObject(expectedObject));
    assertThat(gson.fromJson("null", JSONObject.class)).isNull();
  }

  // Important: Make sure this class is in-sync with the code in Troubleshooting.md
  /**
   * Custom {@code TypeAdapterFactory} for {@link JSONArray} and {@link JSONObject},
   * which uses a format similar to what Gson's reflection-based adapter would have
   * used.
   *
   * <p>This factory is mainly intended for applications which in the past by accident
   * relied on Gson's reflection-based adapter for {@code JSONArray} and {@code JSONObject}
   * and now have to keep this format for backward compatibility.
   */
  private static class JsonOrgBackwardCompatibleAdapterFactory implements TypeAdapterFactory {
    private abstract static class JsonOrgBackwardCompatibleAdapter<W, T> extends TypeAdapter<T> {
      /** Internal field name used by JSON-java for the respective JSON value class */
      private final String fieldName;
      private final TypeAdapter<W> wrappedTypeAdapter;

      public JsonOrgBackwardCompatibleAdapter(String fieldName, TypeAdapter<W> wrappedTypeAdapter) {
        this.fieldName = fieldName;
        this.wrappedTypeAdapter = wrappedTypeAdapter;
      }

      protected abstract T createJsonOrgValue(W wrapped) throws JSONException;

      @Override
      public T read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
          in.nextNull();
          return null;
        }

        in.beginObject();
        String name = in.nextName();
        if (!name.equals(fieldName)) {
          throw new IllegalArgumentException("Unexpected name '" + name + "', expected '" + fieldName + "' at " + in.getPath());
        }
        T value;
        try {
          value = createJsonOrgValue(wrappedTypeAdapter.read(in));
        }
        // For Android this is a checked exception; for the latest JSON-java artifacts it isn't anymore
        catch (JSONException e) {
          throw new RuntimeException(e);
        }
        in.endObject();

        return value;
      }

      protected abstract W getWrapped(T value);

      @Override
      public void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
          out.nullValue();
          return;
        }

        out.beginObject();
        out.name(fieldName);
        wrappedTypeAdapter.write(out, getWrapped(value));
        out.endObject();
      }
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      Class<?> rawType = type.getRawType();

      // Note: This handling for JSONObject.NULL is not the same as the previous Gson reflection-based
      // behavior which would have written `{}`, but this implementation here probably makes more sense
      if (rawType == JSONObject.NULL.getClass()) {
        return new TypeAdapter<T>() {
          @Override
          public T read(JsonReader in) throws IOException {
            in.nextNull();
            return null;
          }

          @Override
          public void write(JsonWriter out, T value) throws IOException {
            out.nullValue();
          }
        };
      }

      if (rawType != JSONArray.class && rawType != JSONObject.class) {
        return null;
      }

      TypeAdapter<?> adapter;
      if (rawType == JSONArray.class) {
        TypeAdapter<List<Object>> wrappedAdapter = gson.getAdapter(new TypeToken<List<Object>> () {});
        adapter = new JsonOrgBackwardCompatibleAdapter<List<Object>, JSONArray>("myArrayList", wrappedAdapter) {
          @Override
          protected JSONArray createJsonOrgValue(List<Object> wrapped) throws JSONException {
            JSONArray jsonArray = new JSONArray();
            // Unlike JSONArray(Collection) constructor, `put` does not wrap elements and is therefore closer
            // to original Gson reflection-based behavior
            for (Object element : wrapped) {
              jsonArray.put(element);
            }

            return jsonArray;
          }

          @Override
          protected List<Object> getWrapped(JSONArray jsonArray) {
            // Cannot use JSONArray.toList() because that converts elements
            List<Object> list = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
              // Use opt(int) because get(int) cannot handle null values
              Object element = jsonArray.opt(i);
              list.add(element);
            }

            return list;
          }
        };
      } else {
        TypeAdapter<Map<String, Object>> wrappedAdapter = gson.getAdapter(new TypeToken<Map<String, Object>> () {});
        adapter = new JsonOrgBackwardCompatibleAdapter<Map<String, Object>, JSONObject>("map", wrappedAdapter) {
          @Override
          protected JSONObject createJsonOrgValue(Map<String, Object> map) throws JSONException {
            // JSONObject(Map) constructor wraps elements, so instead put elements separately to be closer
            // to original Gson reflection-based behavior
            JSONObject jsonObject = new JSONObject();
            for (Entry<String, Object> entry : map.entrySet()) {
              jsonObject.put(entry.getKey(), entry.getValue());
            }

            return jsonObject;
          }

          @Override
          protected Map<String, Object> getWrapped(JSONObject jsonObject) {
            // Cannot use JSONObject.toMap() because that converts elements
            Map<String, Object> map = new LinkedHashMap<>(jsonObject.length());
            @SuppressWarnings("unchecked") // Old JSON-java versions return just `Iterator` instead of `Iterator<String>`
            Iterator<String> names = jsonObject.keys();
            while (names.hasNext()) {
              String name = names.next();
              // Use opt(String) because get(String) cannot handle null values
              // Most likely null values cannot occur normally though; they would be JSONObject.NULL
              map.put(name, jsonObject.opt(name));
            }

            return map;
          }
        };
      }

      // Safe due to type check at beginning of method
      @SuppressWarnings("unchecked")
      TypeAdapter<T> t = (TypeAdapter<T>) adapter;
      return t;
    }
  }

  /**
   * Tests usage of custom adapters for {@link JSONArray} and {@link JSONObject},
   * which serialize and deserialize these classes in (nearly) the same format which the
   * reflection-based adapter would use for them.
   *
   * <p>This test also verifies that the code shown in {@code Troubleshooting.md} works
   * as expected.
   */
  @Test
  public void testCustomBackwardCompatibleAdapters() throws JSONException {
    Gson gson = new GsonBuilder()
        .serializeNulls()
        .registerTypeAdapterFactory(new JsonOrgBackwardCompatibleAdapterFactory())
        .create();

    JSONArray array = new JSONArray(Arrays.asList(
        null,
        JSONObject.NULL,
        123.4,
        true,
        new JSONObject(Collections.singletonMap("key", 1)),
        new JSONArray(Arrays.asList(2)),
        Collections.singletonMap("key", 3),
        Arrays.asList(4),
        new boolean[] {false}
    ));
    assertThat(gson.toJson(array)).isEqualTo(
        "{\"myArrayList\":[null,null,123.4,true,{\"map\":{\"key\":1}},{\"myArrayList\":[2]},{\"key\":3},[4],[false]]}");
    assertThat(gson.toJson(null, JSONArray.class)).isEqualTo("null");

    JSONObject object = new JSONObject();
    object.put("1", JSONObject.NULL);
    object.put("2", 123.4);
    object.put("3", true);
    object.put("4", new JSONObject(Collections.singletonMap("key", 1)));
    object.put("5", new JSONArray(Arrays.asList(2)));
    object.put("6", Collections.singletonMap("key", 3));
    object.put("7", Arrays.asList(4));
    object.put("8", new boolean[] {false});
    assertThat(gson.toJson(object)).isEqualTo(
        "{\"map\":{\"1\":null,\"2\":123.4,\"3\":true,\"4\":{\"map\":{\"key\":1}},\"5\":{\"myArrayList\":[2]},\"6\":{\"map\":{\"key\":3}},\"7\":{\"myArrayList\":[4]},\"8\":[false]}}");
    assertThat(gson.toJson(null, JSONObject.class)).isEqualTo("null");

    ExpectedJSONArray expectedArray = new ExpectedJSONArray(Arrays.asList(
        null,
        true,
        12.0,
        "string",
        Collections.singletonMap("key", 1.0),
        // Nested JSONObject cannot be restored properly
        Collections.singletonMap("map", Collections.singletonMap("key", 2.0)),
        Arrays.asList(3.0),
        // Nested JSONArray cannot be restored properly
        Collections.singletonMap("myArrayList", Arrays.asList(4.0))
    ));
    String json = "{\"myArrayList\": [null, true, 12, \"string\", {\"key\": 1}, {\"map\": {\"key\": 2}}, [3], {\"myArrayList\": [4]}]}";
    JSONArraySubject.assertThat(gson.fromJson(json, JSONArray.class)).isEqualTo(expectedArray);
    assertThat(gson.fromJson("null", JSONArray.class)).isNull();

    Map<String, Object> expectedObject = new HashMap<>();
    expectedObject.put("1", true);
    expectedObject.put("2", 12.0);
    expectedObject.put("3", "string");
    expectedObject.put("4", Collections.singletonMap("key", 1.0));
    // Nested JSONObject cannot be restored properly
    expectedObject.put("5", Collections.singletonMap("map", Collections.singletonMap("key", 2.0)));
    expectedObject.put("6", Arrays.asList(3.0));
    // Nested JSONArray cannot be restored properly
    expectedObject.put("7", Collections.singletonMap("myArrayList", Arrays.asList(4.0)));
    json = "{\"map\": {\"1\": true, \"2\": 12, \"3\": \"string\", \"4\": {\"key\": 1}, \"5\": {\"map\": {\"key\": 2}}, \"6\": [3], \"7\": {\"myArrayList\": [4]}}}";
    JSONObjectSubject.assertThat(gson.fromJson(json, JSONObject.class)).isEqualTo(new ExpectedJSONObject(expectedObject));
    assertThat(gson.fromJson("null", JSONObject.class)).isNull();
  }
}
