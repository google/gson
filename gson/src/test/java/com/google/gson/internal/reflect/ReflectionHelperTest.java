package com.google.gson.internal.reflect;

import org.junit.*;

import java.lang.reflect.Constructor;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;

import static org.junit.Assert.*;

public class ReflectionHelperTest {

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
    public void testJava17Record() throws ClassNotFoundException {
        Class<?> unixDomainPrincipalClass = Class.forName("jdk.net.UnixDomainPrincipal");
        // UnixDomainPrincipal is a record
        assertTrue(ReflectionHelper.isRecord(unixDomainPrincipalClass));
        // with 2 fields
        assertArrayEquals(new String[] {"user", "group"}, ReflectionHelper.getRecordComponentNames(unixDomainPrincipalClass));
        // Check canonical constructor
        Constructor<?> constructor = ReflectionHelper.getCanonicalRecordConstructor(unixDomainPrincipalClass);
        assertNotNull(constructor);
        assertArrayEquals(
                new Class<?>[] { UserPrincipal.class, GroupPrincipal.class },
                constructor.getParameterTypes());
    }


}