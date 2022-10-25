package com.google.gson.internal;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * {@link List} which wraps another {@code List} but prevents insertion of
 * {@code null} elements. Methods which only perform checks with the element
 * argument (e.g. {@link #contains(Object)}) do not throw exceptions for
 * {@code null} arguments.
 */
public class NonNullElementWrapperList<E> extends AbstractList<E> implements RandomAccess {
  // Explicitly specify ArrayList as type to guarantee that delegate implements RandomAccess
  private final ArrayList<E> delegate;

  public NonNullElementWrapperList(ArrayList<E> delegate) {
    this.delegate = Objects.requireNonNull(delegate);
  }

  @Override public E get(int index) {
    return delegate.get(index);
  }

  @Override public int size() {
    return delegate.size();
  }

  private E nonNull(E element) {
    if (element == null) {
      throw new NullPointerException("Element must be non-null");
    }
    return element;
  }

  @Override public E set(int index, E element) {
    return delegate.set(index, nonNull(element));
  }

  @Override public void add(int index, E element) {
    delegate.add(index, nonNull(element));
  }

  @Override public E remove(int index) {
    return delegate.remove(index);
  }

  /* The following methods are overridden because their default implementation is inefficient */

  @Override public void clear() {
    delegate.clear();
  }

  @Override public boolean remove(Object o) {
    return delegate.remove(o);
  }

  @Override public boolean removeAll(Collection<?> c) {
    return delegate.removeAll(c);
  }

  @Override public boolean retainAll(Collection<?> c) {
    return delegate.retainAll(c);
  }

  @Override public boolean contains(Object o) {
    return delegate.contains(o);
  }

  @Override public int indexOf(Object o) {
    return delegate.indexOf(o);
  }

  @Override public int lastIndexOf(Object o) {
    return delegate.lastIndexOf(o);
  }

  @Override public Object[] toArray() {
    return delegate.toArray();
  }

  @Override public <T> T[] toArray(T[] a) {
    return delegate.toArray(a);
  }

  @Override public boolean equals(Object o) {
    return delegate.equals(o);
  }

  @Override public int hashCode() {
    return delegate.hashCode();
  }

  // TODO: Once Gson targets Java 8 also override List.sort
}
