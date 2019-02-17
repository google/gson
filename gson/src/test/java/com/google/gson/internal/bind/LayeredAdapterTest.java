package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.common.TestTypes.LayeredConflictOutsideClass;
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
        .registerTypeAdapterWithFillIn(LayeredOutsideClass.class, new OutsideAdapter())
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
        .registerTypeAdapterWithFillIn(LayeredConflictOutsideClass.class,
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
        .registerTypeAdapterWithFillIn(LayeredTypeConflictOutsideClass.class,
            new TypeConflictOutsideAdapter())
        .create();
    String conflictString = "{ \"hello\": \"sample\", \"hello2\": 89}";

    try {
      LayeredTypeConflictOutsideClass outside = gson
          .fromJson(conflictString, LayeredTypeConflictOutsideClass.class);
      fail();
    } catch (JsonSyntaxException e) {
      Assert.assertEquals(ParseException.class, e.getCause().getClass());
      Assert.assertEquals(NumberFormatException.class, e.getCause().getCause().getClass());
    }
  }

  @Test
  public void testConflictingTypesWithExposeAnnotation() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapterWithFillIn(LayeredTypeConflictOutsideClass.class,
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

  public class OutsideAdapter extends TypeAdapter<LayeredOutsideClass> {

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
        if (name.equals("hello")) {
          inside.setHello(in.nextString());
        } else if (name.equals("hello2")) {
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

  public class ConflictOutsideAdapter extends TypeAdapter<LayeredConflictOutsideClass> {

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
        if (name.equals("hello")) {
          inside.setHello(in.nextString());
        } else if (name.equals("hello2")) {
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

  public class TypeConflictOutsideAdapter extends TypeAdapter<LayeredTypeConflictOutsideClass> {

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
        if (name.equals("hello")) {
          inside.setHello(in.nextString());
        } else if (name.equals("hello2")) {
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
}
