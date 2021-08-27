package com.google.gson;

import com.google.gson.internal.Excluder;
import junit.framework.TestCase;

public class AnonymousAndLocalClassInclusionStrategyTest extends TestCase {
    private Anonymous anonymousClass = new Anonymous() {
        @Override
        public void method() {
            //intentionally left blank
        }
    };

    private Excluder excluderWithAnonymousAndLocalClassSerializationEnabled = Excluder.DEFAULT.enableAnonymousAndLocalClassSerialization();
    private Excluder defaultExcluder = Excluder.DEFAULT;

    public void testDoNotExcludeLocalClassObject() throws Exception {
        class LocalClass {

        }

        LocalClass localClass = new LocalClass();
        Class<?> clazz = localClass.getClass();

        /*
         * Since the serialization of local and anonymous classes is enabled,
         * this class should not be excluded.
         */
        assertFalse(excluderWithAnonymousAndLocalClassSerializationEnabled.excludeClass(clazz, true));
    }

    public void testDoNotExcludeLocalClassField() throws Exception {
        class LocalClass {
            public String name;
        }

        LocalClass localClass = new LocalClass();
        localClass.name = "localClassField";
        Class<?> clazz = localClass.getClass();

        /*
         * Since the serialization of local and anonymous classes is enabled,
         * this class should not be excluded.
         */
        assertFalse(excluderWithAnonymousAndLocalClassSerializationEnabled.excludeField(clazz.getField("name"), true));
    }

    public void testExcludeLocalClassObject() throws Exception {
        class LocalClass {

        }

        LocalClass localClass = new LocalClass();
        Class<?> clazz = localClass.getClass();

        /*
         * Since the serialization of local and anonymous classes is disabled by default,
         * this class should be excluded.
         */
        assertTrue(defaultExcluder.excludeClass(clazz, true));
    }

    public void testExcludeLocalClassField() throws Exception {
        class AnotherLocalClass {

        }

        class LocalClass {
            public AnotherLocalClass anotherLocalClass;
        }

        LocalClass localClass = new LocalClass();
        Class<?> clazz = localClass.getClass();

         /*
         * Since the serialization of local and anonymous classes is disabled by default,
         * this class should be excluded.
         */
        assertTrue(defaultExcluder.excludeField(clazz.getField("anotherLocalClass"), true));
    }

    public void testDoNotExcludeAnonymousClassObject() throws Exception {
        Class<?> clazz = anonymousClass.getClass();

        /*
         * Since the serialization of local and anonymous classes is enabled,
         * this class should not be excluded.
         */
        assertFalse(excluderWithAnonymousAndLocalClassSerializationEnabled.excludeClass(clazz, true));
    }

    public void testExcludeAnonymousClassObject() throws Exception {
        Class<?> clazz = anonymousClass.getClass();

         /*
         * Since the serialization of local and anonymous classes is disabled by default,
         * this class should be excluded.
         */
        assertTrue(defaultExcluder.excludeClass(clazz, true));
    }

    interface Anonymous {
        void method();
    }
}
