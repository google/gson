package com.google.gson;

import com.google.gson.annotations.Ignore;
import com.google.gson.internal.Excluder;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Created by Rao-Mengnan
 * on 2018/3/1.
 */
public class IgnoreAnnotationExclusionStrategyTest {

    private Excluder excluder = Excluder.DEFAULT.excludeFieldsWithIgnoreAnnotation();
    @Test
    public void testSkipAnnotationField() throws Exception {
        Field f = getField("ignoredField");
        assertTrue(excluder.excludeField(f, true));
        assertTrue(excluder.excludeField(f, false));
    }

    @Test
    public void testIgnoreSerialize() throws Exception {
        Field f = getField("ignoreSerializeField");
        assertTrue(excluder.excludeField(f, true));
        assertFalse(excluder.excludeField(f, false));
    }

    @Test
    public void testIgnoreDeserialize() throws Exception {
        Field f = getField("ignoreDeserializeField");
        assertFalse(excluder.excludeField(f, true));
        assertTrue(excluder.excludeField(f, false));
    }

    @Test
    public void testExplicitlyExposed() throws Exception {
        Field f = getField("explicitlyExposedField");
        assertFalse(excluder.excludeField(f, true));
        assertFalse(excluder.excludeField(f, false));
    }

    private Field getField(String name) throws Exception {
        return TestObject.class.getField(name);
    }

    @SuppressWarnings("unused")
    private static class TestObject {
        @Ignore
        public final int ignoredField = 0;

        @Ignore(serialize=true, deserialize=false)
        public final int ignoreSerializeField = 0;

        @Ignore(serialize=false, deserialize=true)
        public final int ignoreDeserializeField = 0;

        public final int explicitlyExposedField = 0;
    }
}
