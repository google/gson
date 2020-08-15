package com.google.gson.internal.bind;

import java.io.EOFException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.ReadOnlyBufferException;

import com.google.gson.stream.JsonReader.StringValueReader;

/**
 * Abstract base class for {@link StringValueReader}s which read a {@code String}.
 * Subclasses only have to override {@link #onClosedAfterReachedEnd()}.
 */
public abstract class AbstractStringValueReaderStringImpl extends StringValueReader {
  /** The value this reader is reading from. */
  protected final String value;
  /**
   * {@code true} if this value represents a JSON property name;
   * {@code false} if it represents a JSON string value.
   */
  protected final boolean isName;
  private int index = 0;
  private boolean isClosed = false;

  public AbstractStringValueReaderStringImpl(String value, boolean isName) {
    this.value = value;
    this.isName = isName;
  }

  private void verifyNotClosed() throws IOException {
    if (isClosed) {
      throw new IOException("Reader is closed");
    }
  }

  private boolean reachedEnd() {
    return index >= value.length();
  }

  private int remaining() {
    return value.length() - index;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    return readGreedily(cbuf, off, len);
  }

  @Override
  public int readGreedily(char[] cbuf, int off, int len) throws IOException {
    if (off < 0) {
      throw new IndexOutOfBoundsException("offset < 0");
    } else if (len < 0) {
      throw new IndexOutOfBoundsException("length < 0");
    } else if (len > cbuf.length - off) {
      throw new IndexOutOfBoundsException("length > arr.length - offset");
    }
    verifyNotClosed();

    if (len == 0) {
      return 0;
    } else if (reachedEnd()) {
      return -1;
    } else {
      int readAmount = Math.min(remaining(), len);
      value.getChars(index, index + readAmount, cbuf, off);
      index += readAmount;
      return readAmount;
    }
  }

  @Override
  public int read() throws IOException {
    verifyNotClosed();

    if (reachedEnd()) {
      return -1;
    } else {
      return value.charAt(index++);
    }
  }

  @Override
  public int read(CharBuffer target) throws IOException {
    if (target.isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    verifyNotClosed();

    if (reachedEnd()) {
      return -1;
    }

    int readAmount = Math.min(remaining(), target.remaining());
    target.put(value, index, index + readAmount);
    index += readAmount;
    return readAmount;
  }

  @Override
  public int readAtLeast(char[] cbuf, int off, int minLen, int maxLen) throws IOException {
    if (off < 0) {
      throw new IndexOutOfBoundsException("offset < 0");
    } else if (minLen < 0) {
      throw new IndexOutOfBoundsException("minLen < 0");
    } else if (minLen > maxLen) {
      throw new IndexOutOfBoundsException("minLen > maxLen");
    } else if (maxLen > cbuf.length - off) {
      throw new IndexOutOfBoundsException("maxLen > arr.length - offset");
    }
    verifyNotClosed();

    if (maxLen == 0) {
      return 0;
    } else if (minLen > remaining()) {
      throw new EOFException("Less than the requested " + minLen + " chars are remaining");
    } else if (reachedEnd()) {
      return 0;
    } else {
      int readAmount = Math.min(remaining(), maxLen);
      value.getChars(index, index + readAmount, cbuf, off);
      index += readAmount;
      return readAmount;
    }
  }

  @Override
  public long skip(long skipAmount) throws IOException {
    skipAmount = Math.min(skipAmount, remaining());
    skipExactly(skipAmount);
    return skipAmount;
  }

  @Override
  public void skipExactly(long skipAmount) throws IOException {
    if (skipAmount < 0) {
      throw new IllegalArgumentException("skip amount is negative");
    }
    verifyNotClosed();

    if (skipAmount > remaining()) {
      throw new EOFException("Less than the requested " + skipAmount + " chars are remaining");
    }
    index += skipAmount;
  }

  @Override
  public void skipRemaining() throws IOException {
    verifyNotClosed();
    index = value.length();
  }

  @Override
  public boolean ready() throws IOException {
    verifyNotClosed();

    // Always return true, even if end has been reached, see also JDK-8196767
    return true;
  }

  @Override
  public final void close() throws IOException {
    if (!isClosed) {
      isClosed = true;

      // Update enclosing JsonReader, but only if end has been reached
      // to be consistent with JsonReader's string value reader
      if (reachedEnd()) {
        onClosedAfterReachedEnd();
      }
    }
  }

  /**
   * Called when {@link #close()} is called and the reader has reached
   * the end of the string.
   */
  protected abstract void onClosedAfterReachedEnd();
}

