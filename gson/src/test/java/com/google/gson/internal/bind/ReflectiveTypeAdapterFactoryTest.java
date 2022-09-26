package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.util.Objects;

import static org.junit.Assert.*;

public class ReflectiveTypeAdapterFactoryTest {


    @Before
    public void setUp() throws Exception {
        try {
            Class.forName("java.lang.Record");
        } catch (ClassNotFoundException e) {
            // Records not supported, ignore
            throw new AssumptionViolatedException("java.lang.Record not supported");
        }
    }

    @Test
    public void testCustomAdapterForRecords() throws ClassNotFoundException {
        Class<?> unixDomainPrincipalClass = Class.forName("jdk.net.UnixDomainPrincipal");

        Gson gson = new Gson();
        TypeAdapter<?> recordAdapter = gson.getAdapter(unixDomainPrincipalClass);
        TypeAdapter<?> defaultReflectionAdapter = gson.getAdapter(GroupPrincipalImpl.class);
        assertNotEquals(recordAdapter.getClass(), defaultReflectionAdapter.getClass());
    }

    @Test
    public void testSerializeRecords() throws ReflectiveOperationException {
        Class<?> unixDomainPrincipalClass = Class.forName("jdk.net.UnixDomainPrincipal");
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(UserPrincipal.class, new TypeAdapter<UserPrincipal>() {
                    @Override
                    public void write(JsonWriter out, UserPrincipal principal) throws IOException {
                        out.value(principal.getName());
                    }

                    @Override
                    public UserPrincipal read(JsonReader in) throws IOException {
                        final String name = in.nextString();
                        return new UserPrincipalImpl(name);
                    }
                })
                .registerTypeAdapter(GroupPrincipal.class, new TypeAdapter<GroupPrincipal>() {
                    @Override
                    public void write(JsonWriter out, GroupPrincipal value) throws IOException {
                        out.value(value.getName());
                    }

                    @Override
                    public GroupPrincipal read(JsonReader in) throws IOException {
                        final String name = in.nextString();
                        return new GroupPrincipalImpl(name);
                    }
                })
                .create();
        UserPrincipal userPrincipal = gson.fromJson("\"user\"", UserPrincipal.class);
        GroupPrincipal groupPrincipal = gson.fromJson("\"group\"", GroupPrincipal.class);
        Object recordInstance = unixDomainPrincipalClass.getDeclaredConstructor(UserPrincipal.class, GroupPrincipal.class)
                .newInstance(userPrincipal, groupPrincipal);
        String serialized = gson.toJson(recordInstance);
        Object deserializedRecordInstance = gson.fromJson(serialized, unixDomainPrincipalClass);

        assertEquals(recordInstance, deserializedRecordInstance);
    }

    private static class GroupPrincipalImpl implements GroupPrincipal {
        private final String _name;

        public GroupPrincipalImpl(String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupPrincipalImpl that = (GroupPrincipalImpl) o;
            return Objects.equals(_name, that._name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(_name);
        }
    }

    private static class UserPrincipalImpl implements UserPrincipal {
        private final String _name;

        public UserPrincipalImpl(String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserPrincipalImpl that = (UserPrincipalImpl) o;
            return Objects.equals(_name, that._name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(_name);
        }
    }
}