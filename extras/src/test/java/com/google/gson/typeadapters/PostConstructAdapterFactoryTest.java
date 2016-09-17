/*
 * Copyright (C) 2016 Gson Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.typeadapters;

import javax.annotation.PostConstruct;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import junit.framework.TestCase;

public class PostConstructAdapterFactoryTest extends TestCase {
    public void test() throws Exception {
        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new PostConstructAdapterFactory())
                .create();
        gson.fromJson("{\"bread\": \"white\", \"cheese\": \"cheddar\"}", Sandwich.class);
        try {
            gson.fromJson("{\"bread\": \"cheesey bread\", \"cheese\": \"swiss\"}", Sandwich.class);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("too cheesey", expected.getMessage());
        }
    }

    static class Sandwich {
        String bread;
        String cheese;

        @PostConstruct void validate() {
            if (bread.equals("cheesey bread") && cheese != null) {
                throw new IllegalArgumentException("too cheesey");
            }
        }
    }
}
