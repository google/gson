package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.TestCase;

/**
 * Functional Test exercising serialization/deserialization using getter/setter methods.
 *
 * @author Raj Srivastava
 */
public class UseGetterSetterTest extends TestCase {

  private static final int    INITIAL_INT    = 100;
  private static final int    GETTER_OFFSET  = 20;
  private static final int    SETTER_OFFSET  = 40;
  private static final String INITIAL_STRING = "initial";
  private static final String GETTER_SUFFIX  = "-g";
  private static final String SETTER_SUFFIX  = "-s";

  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new GsonBuilder().useGetterSetter().create();
  }

  public void testGetterSetterUse() throws Exception {
    ClassWithGetterSetter request = new ClassWithGetterSetter(INITIAL_INT, INITIAL_STRING);
    String json = gson.toJson(request);

    ClassWithGetterSetter response = gson.fromJson(json, ClassWithGetterSetter.class);
    assertEquals(response.primitiveField, INITIAL_INT + GETTER_OFFSET + SETTER_OFFSET);
    assertEquals(response.nonPrimitiveField, INITIAL_STRING + GETTER_SUFFIX + SETTER_SUFFIX);
  }

  public static class ClassWithGetterSetter {
    int     primitiveField;
    String  nonPrimitiveField;

    public ClassWithGetterSetter(int primitiveField, String nonPrimitiveField) {
      this.primitiveField = primitiveField;
      this.nonPrimitiveField = nonPrimitiveField;
    }

    public int getPrimitiveField() {
      return primitiveField + GETTER_OFFSET;
    }

    public void setPrimitiveField(int primitiveField) {
      this.primitiveField = primitiveField + SETTER_OFFSET;
    }

    public String getNonPrimitiveField() {
      return nonPrimitiveField + GETTER_SUFFIX;
    }

    public void setNonPrimitiveField(String nonPrimitiveField) {
      this.nonPrimitiveField = nonPrimitiveField + SETTER_SUFFIX;
    }
  }
}
