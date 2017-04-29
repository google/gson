package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;
import junit.framework.TestCase;

import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;

/**
 * Test fixes for infinite recursion on {@link $Gson$Types#resolve(Type, Class, Type)}, described at
 * <a href="https://github.com/google/gson/issues/440">Issue #440</a> and similar issues.
 * <p>
 * These tests originally caused {@link StackOverflowError} because of infinite recursion on attempts to
 * resolve generics on types, with an intermediate types like 'Foo2&lt;? extends ? super ? extends ... ? extends A&gt;'
 */
public class RecursiveTypesResolveTest extends TestCase {

  private static class Foo1<A> {
    Foo2<? extends A> foo2;
  }

  private static class Foo2<B> {
    Foo1<? super B> foo1;
  }

  /**
   * Test simplest case of recursion.
   */
  public void testRecursiveResolveSimple() {
    new Gson().getAdapter(Foo1.class);
  }

  //
  // Real-world samples, found in Issues #603 and #440.
  //
  public void testIssue603_PrintStream() {
    new Gson().getAdapter(PrintStream.class);
  }

  public void testIssue440_WeakReference() throws Exception {
    new Gson().getAdapter(WeakReference.class);
  }

  //
  // Tests belows check the behaviour of the methods changed for the fix
  //

  public void testDoubleSupertype() {
    assertEquals($Gson$Types.supertypeOf(Number.class),
            $Gson$Types.supertypeOf($Gson$Types.supertypeOf(Number.class)));
  }

  public void testDoubleSubtype() {
    assertEquals($Gson$Types.subtypeOf(Number.class),
            $Gson$Types.subtypeOf($Gson$Types.subtypeOf(Number.class)));
  }

  public void testSuperSubtype() {
    assertEquals($Gson$Types.subtypeOf(Object.class),
            $Gson$Types.supertypeOf($Gson$Types.subtypeOf(Number.class)));
  }

  public void testSubSupertype() {
    assertEquals($Gson$Types.subtypeOf(Object.class),
            $Gson$Types.subtypeOf($Gson$Types.supertypeOf(Number.class)));
  }
}
