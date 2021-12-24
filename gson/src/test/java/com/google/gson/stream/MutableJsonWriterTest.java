/*
 * Copyright (C) 2010 Google Inc.
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

package com.google.gson.stream;

import java.io.StringWriter;
import junit.framework.TestCase;

public class MutableJsonWriterTest extends TestCase{
    
    public void testMutability() throws Exception{
        MutableJsonWriter mutableJsonWriter = new MutableJsonWriter();
        final String keyA = "happy";
        final String keyB = "opensource";
        
        String value = "911";
        String expectedJsonString=String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}", keyA,value,keyB,value);
        StringWriter stringWriter = new StringWriter();
        mutableJsonWriter.reset(stringWriter);
        mutableJsonWriter.beginObject();
        mutableJsonWriter.name(keyA);
        mutableJsonWriter.value(value);
        mutableJsonWriter.name(keyB);
        mutableJsonWriter.value(value);
        mutableJsonWriter.endObject();
        mutableJsonWriter.flush();
        mutableJsonWriter.close();
        String generatedJsonString = stringWriter.toString();
//        System.out.printf("gen json: %s\n",generatedJsonString);
        assertEquals(expectedJsonString, generatedJsonString);
        
        value = "992";
        expectedJsonString=String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}", keyA,value,keyB,value);
        stringWriter = new StringWriter();
        mutableJsonWriter.reset(stringWriter);
        mutableJsonWriter.beginObject();
        mutableJsonWriter.name(keyA);
        mutableJsonWriter.value(value);
        mutableJsonWriter.name(keyB);
        mutableJsonWriter.value(value);
        mutableJsonWriter.endObject();
        mutableJsonWriter.flush();
        mutableJsonWriter.close();
        generatedJsonString = stringWriter.toString();
//        System.out.printf("gen json: %s\n",generatedJsonString);
        assertEquals(expectedJsonString, generatedJsonString);
    }
    
}
