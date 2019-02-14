package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import junit.framework.TestCase;
import org.junit.Assert;

public class LayeredAdapterTest extends TestCase {

  private Gson gson;

  public class Inside {
    private String hello;
    private Integer hello2;

    public String getHello() {
      return hello;
    }

    public void setHello(String hello) {
      this.hello = hello;
    }

    public Integer getHello2() {
      return hello2;
    }

    public void setHello2(Integer hello2) {
      this.hello2 = hello2;
    }
  }

  public class Outside {
    private String field1;
    private Integer field2;
    private Inside insideClass;

    public String getField1() {
      return field1;
    }

    public void setField1(String field1) {
      this.field1 = field1;
    }

    public Integer getField2() {
      return field2;
    }

    public void setField2(Integer field2) {
      this.field2 = field2;
    }

    public Inside getInsideClass() {
      return insideClass;
    }

    public void setInsideClass(Inside insideClass) {
      this.insideClass = insideClass;
    }
  }

  public class OutsideAdapter extends TypeAdapter<Outside> {

    @Override
    public void write(JsonWriter out, Outside value) throws IOException {
      // we don't care about this method. Write will happen the normal way that writes happen.
    }

    @Override
    public Outside read(JsonReader in) throws IOException {
      if (in.peek()== JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      in.beginObject();

      Inside inside = new Inside();
      Outside outside = new Outside();

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

  @Override
  public void setUp() throws Exception {
    super.setUp();
    gson = new GsonBuilder()
        .registerTypeAdapterWithFillIn(Outside.class, new OutsideAdapter())
        .create();
  }

  public void testBasicLayering() {
    String basicString = "{ \"field1\": \"sample\", \"field2\": 5, \"hello\": \"hi there\", \"hello2\": 6}";
    Outside outside = gson.fromJson(basicString, Outside.class);

    Assert.assertEquals("sample", outside.getField1());
    Assert.assertEquals(Integer.valueOf(5), outside.getField2());
    Assert.assertEquals("hi there", outside.getInsideClass().getHello());
    Assert.assertEquals(Integer.valueOf(6), outside.getInsideClass().getHello2());
  }
}
