package com.google.gson.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Writer;
import org.junit.Test;

public class StreamsTest {
  @Test
  public void testWriterForAppendable() throws IOException {
    StringBuilder stringBuilder = new StringBuilder();
    Writer writer = Streams.writerForAppendable(stringBuilder);

    writer.append('a');
    writer.append('\u1234');
    writer.append("test");
    writer.append(null); // test custom null handling mandated by `append`
    writer.append("abcdef", 2, 4);
    writer.append(null, 1, 3); // test custom null handling mandated by `append`
    writer.append(',');

    writer.write('a');
    writer.write('\u1234');
    // Should only consider the 16 low-order bits
    writer.write(0x4321_1234);
    writer.append(',');

    writer.write("chars".toCharArray());
    try {
      writer.write((char[]) null);
      fail();
    } catch (NullPointerException e) {
    }

    writer.write("chars".toCharArray(), 1, 2);
    try {
      writer.write((char[]) null, 1, 2);
      fail();
    } catch (NullPointerException e) {
    }
    writer.append(',');

    writer.write("string");
    try {
      writer.write((String) null);
      fail();
    } catch (NullPointerException e) {
    }

    writer.write("string", 1, 2);
    try {
      writer.write((String) null, 1, 2);
      fail();
    } catch (NullPointerException e) {
    }

    String actualOutput = stringBuilder.toString();
    assertEquals("a\u1234testnullcdul,a\u1234\u1234,charsha,stringtr", actualOutput);

    writer.flush();
    writer.close();

    // flush() and close() calls should have had no effect
    assertEquals(actualOutput, stringBuilder.toString());
  }
}
