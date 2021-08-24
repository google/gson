package com.google.gson.internal.bind;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.ReflectiveTypeAdapter;
import com.google.gson.TypeAdapter;
import com.google.gson.common.TestTypes.LayeredConflictOutsideClass;
import com.google.gson.common.TestTypes.LayeredFieldNamingClass;
import com.google.gson.common.TestTypes.LayeredInsideClass;
import com.google.gson.common.TestTypes.LayeredOutsideClass;
import com.google.gson.common.TestTypes.LayeredTypeConflictOutsideClass;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.text.ParseException;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class LayeredAdapterTest extends TestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void testBasicLayering() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(LayeredOutsideClass.class, new OutsideAdapter())
        .create();
    String basicString = "{ \"field1\": \"sample\", \"field2\": 5, \"hello\": \"hi there\", \"hello2\": 6}";
    LayeredOutsideClass outside = gson.fromJson(basicString, LayeredOutsideClass.class);

    Assert.assertEquals("sample", outside.getField1());
    Assert.assertEquals(Integer.valueOf(5), outside.getField2());
    Assert.assertEquals("hi there", outside.getInsideClass().getHello());
    Assert.assertEquals(Integer.valueOf(6), outside.getInsideClass().getHello2());
  }

  @Test
  public void testConflictingNames() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(LayeredConflictOutsideClass.class,
            new ConflictOutsideAdapter())
        .create();
    String conflictString = "{ \"hello\": \"sample\", \"hello2\": 89}";

    LayeredConflictOutsideClass outside = gson
        .fromJson(conflictString, LayeredConflictOutsideClass.class);
    Assert.assertEquals("sample", outside.getInsideClass().getHello());
    Assert.assertEquals(Integer.valueOf(89), outside.getInsideClass().getHello2());
    Assert.assertEquals("sample", outside.getHello());
    Assert.assertEquals(89, outside.getHello2());
  }

  @Test
  public void testConflictingTypes() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(LayeredTypeConflictOutsideClass.class,
            new TypeConflictOutsideAdapter())
        .create();
    String conflictString = "{ \"hello\": \"sample\", \"hello2\": 89}";

    try {
      // Just check that there is an error thrown when this method is called
      gson.fromJson(conflictString, LayeredTypeConflictOutsideClass.class);
      fail();
    } catch (JsonSyntaxException e) {
      Assert.assertEquals(ParseException.class, e.getCause().getClass());
      Assert.assertEquals(NumberFormatException.class, e.getCause().getCause().getClass());
    }
  }

  @Test
  public void testConflictingTypesWithExposeAnnotation() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(LayeredTypeConflictOutsideClass.class,
            new TypeConflictOutsideAdapter())
        .excludeFieldsWithoutExposeAnnotation()
        .create();
    String conflictString = "{ \"hello\": \"sample\", \"hello2\": 89}";

    LayeredTypeConflictOutsideClass outside = gson
        .fromJson(conflictString, LayeredTypeConflictOutsideClass.class);
    Assert.assertEquals("sample", outside.getInsideClass().getHello());
    Assert.assertEquals(Integer.valueOf(89), outside.getInsideClass().getHello2());
    Assert.assertNull(outside.getHello());
    Assert.assertNull(outside.getHello2());
  }

  @Test
  public void testFieldNamingStrategy() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(LayeredFieldNamingClass.class,
            new LayeredFieldNamingAdapter())
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create();

    String json =
        "{\"first_string\": \"sample\", \"second_integer\": 87, \"third_date\": \"2019-02-08T08:07:34Z\","
            + "\"weirdlyNameDateObjectThing\": \"sample_text\"}";

    LayeredFieldNamingClass fieldNamingClass = gson.fromJson(json, LayeredFieldNamingClass.class);
    Assert.assertEquals(fieldNamingClass.getFirstString(), "sample");
    Assert.assertEquals(fieldNamingClass.getWeirdlyNamedDate(), "sample_text");
  }

  public class OutsideAdapter extends ReflectiveTypeAdapter<LayeredOutsideClass> {

    @Override
    public void write(JsonWriter out, LayeredOutsideClass value) throws IOException {
      // we don't care about this method. Write will happen the normal way that writes happen.
    }

    @Override
    public LayeredOutsideClass read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      in.beginObject();

      LayeredInsideClass inside = new LayeredInsideClass();
      LayeredOutsideClass outside = new LayeredOutsideClass();

      while (in.hasNext()) {
        String name = in.nextName();
        if ("hello".equals(name)) {
          inside.setHello(in.nextString());
        } else if ("hello2".equals(name)) {
          inside.setHello2(in.nextInt());
        } else {
          in.skipValue();
        }
      }
      in.endObject();

      outside.setInsideClass(inside);
      return outside;
    }
  }

  public class ConflictOutsideAdapter extends ReflectiveTypeAdapter<LayeredConflictOutsideClass> {

    @Override
    public void write(JsonWriter out, LayeredConflictOutsideClass value) throws IOException {
      // we don't care about this method. Write will happen the normal way that writes happen.
    }

    @Override
    public LayeredConflictOutsideClass read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      in.beginObject();

      LayeredInsideClass inside = new LayeredInsideClass();
      LayeredConflictOutsideClass outside = new LayeredConflictOutsideClass();

      while (in.hasNext()) {
        String name = in.nextName();
        if ("hello".equals(name)) {
          inside.setHello(in.nextString());
        } else if ("hello2".equals(name)) {
          inside.setHello2(in.nextInt());
        } else {
          in.skipValue();
        }
      }
      in.endObject();

      outside.setInsideClass(inside);
      return outside;
    }
  }

  public class TypeConflictOutsideAdapter extends ReflectiveTypeAdapter<LayeredTypeConflictOutsideClass> {

    @Override
    public void write(JsonWriter out, LayeredTypeConflictOutsideClass value) throws IOException {
      // we don't care about this method. Write will happen the normal way that writes happen.
    }

    @Override
    public LayeredTypeConflictOutsideClass read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      in.beginObject();

      LayeredInsideClass inside = new LayeredInsideClass();
      LayeredTypeConflictOutsideClass outside = new LayeredTypeConflictOutsideClass();

      while (in.hasNext()) {
        String name = in.nextName();
        if ("hello".equals(name)) {
          inside.setHello(in.nextString());
        } else if ("hello2".equals(name)) {
          inside.setHello2(in.nextInt());
        } else {
          in.skipValue();
        }
      }
      in.endObject();

      outside.setInsideClass(inside);
      return outside;
    }
  }

  public class LayeredFieldNamingAdapter extends ReflectiveTypeAdapter<LayeredFieldNamingClass> {

    @Override
    public void write(JsonWriter out, LayeredFieldNamingClass value) throws IOException {
      // Don't Care
    }

    @Override
    public LayeredFieldNamingClass read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      LayeredFieldNamingClass fieldNamingClass = new LayeredFieldNamingClass();

      in.beginObject();
      String name = "";
      while (in.hasNext()) {
        name = in.nextName();
        if ("weirdlyNameDateObjectThing".equals(name)) {
          fieldNamingClass.setWeirdlyNamedDate(in.nextString());
        } else {
          in.skipValue();
        }
      }

      return fieldNamingClass;
    }
  }
}
