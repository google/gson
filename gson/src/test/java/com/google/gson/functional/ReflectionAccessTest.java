package com.google.gson.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import java.util.Collections;
import org.junit.Test;

public class ReflectionAccessTest {
  /**
   * Test serializing an instance of a non-accessible internal class, but where
   * Gson supports serializing one of its superinterfaces.
   *
   * <p>Here {@link Collections#emptyList()} is used which returns an instance
   * of the internal class {@code java.util.Collections.EmptyList}. Gson should
   * serialize the object as {@code List} despite the internal class not being
   * accessible.
   *
   * <p>See https://github.com/google/gson/issues/1875
   */
  @Test
  public void testSerializeInternalImplementationObject() {
    Gson gson = new Gson();
    String json = gson.toJson(Collections.emptyList());
    assertEquals("[]", json);

    // But deserialization should fail
    Class<?> internalClass = Collections.emptyList().getClass();
    try {
      gson.fromJson("{}", internalClass);
      fail("Missing exception; test has to be run with `--illegal-access=deny`");
    } catch (JsonIOException expected) {
      assertTrue(expected.getMessage().startsWith(
          "Failed making constructor 'java.util.Collections$EmptyList#EmptyList()' accessible; "
          + "either change its visibility or write a custom InstanceCreator or TypeAdapter for its declaring type"
      ));
    }
  }
}
