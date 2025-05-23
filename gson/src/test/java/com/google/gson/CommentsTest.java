/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.reflect.TypeToken;
import java.util.List;
import org.junit.Test;

/**
 * Tests that by default Gson accepts several forms of comments.
 *
 * @author Jesse Wilson
 */
public final class CommentsTest {

  /** Test for issue 212. */
  @Test
  public void testParseComments() {
    String json =
        "[\n"
            + "  // this is a comment\n"
            + "  \"a\",\n"
            + "  /* this is another comment */\n"
            + "  \"b\",\n"
            + "  # this is yet another comment\n"
            + "  \"c\"\n"
            + "]";

    List<String> abc = new Gson().fromJson(json, new TypeToken<List<String>>() {}.getType());
    assertThat(abc).containsExactly("a", "b", "c").inOrder();
  }
}
