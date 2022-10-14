package com.google.gson.internal.bind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.reflect.Java17ReflectionHelperTest;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.security.Principal;
import org.junit.Before;
import org.junit.Test;

public class Java17ReflectiveTypeAdapterFactoryTest {

  // The class jdk.net.UnixDomainPrincipal is one of the few Record types that are included in the JDK.
  // We use this to test serialization and deserialization of Record classes, so we do not need to
  // have record support at the language level for these tests. This class was added in JDK 16.
  Class<?> unixDomainPrincipalClass;

  @Before
  public void setUp() throws Exception {
    unixDomainPrincipalClass = Class.forName("jdk.net.UnixDomainPrincipal");
  }

  // Class for which the normal reflection based adapter is used
  private static class DummyClass {
    @SuppressWarnings("unused")
    public String s;
  }

  @Test
  public void testCustomAdapterForRecords() {
    Gson gson = new Gson();
    TypeAdapter<?> recordAdapter = gson.getAdapter(unixDomainPrincipalClass);
    TypeAdapter<?> defaultReflectionAdapter = gson.getAdapter(DummyClass.class);
    assertNotEquals(recordAdapter.getClass(), defaultReflectionAdapter.getClass());
  }

  @Test
  public void testSerializeRecords() throws ReflectiveOperationException {
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(UserPrincipal.class, new PrincipalTypeAdapter<>())
            .registerTypeAdapter(GroupPrincipal.class, new PrincipalTypeAdapter<>())
            .create();

    UserPrincipal userPrincipal = gson.fromJson("\"user\"", UserPrincipal.class);
    GroupPrincipal groupPrincipal = gson.fromJson("\"group\"", GroupPrincipal.class);
    Object recordInstance =
        unixDomainPrincipalClass
            .getDeclaredConstructor(UserPrincipal.class, GroupPrincipal.class)
            .newInstance(userPrincipal, groupPrincipal);
    String serialized = gson.toJson(recordInstance);
    Object deserializedRecordInstance = gson.fromJson(serialized, unixDomainPrincipalClass);

    assertEquals(recordInstance, deserializedRecordInstance);
    assertEquals("{\"user\":\"user\",\"group\":\"group\"}", serialized);
  }

  private static class PrincipalTypeAdapter<T extends Principal> extends TypeAdapter<T> {
    @Override
    public void write(JsonWriter out, T principal) throws IOException {
      out.value(principal.getName());
    }

    @Override
    public T read(JsonReader in) throws IOException {
      final String name = in.nextString();
      // This type adapter is only used for Group and User Principal, both of which are implemented by PrincipalImpl.
      @SuppressWarnings("unchecked")
      T principal = (T) new Java17ReflectionHelperTest.PrincipalImpl(name);
      return principal;
    }
  }
}
