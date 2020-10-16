/*
 * Copyright 2020 https://github.com/911992.
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

/*
gson
File: MutableJsonReaderTest.java
Created on: Oct 10, 2020 10:11:11 AM
    @author https://github.com/911992
 
History:
    initial version: 0.1(20201010)
*/

package com.google.gson.stream;

import junit.framework.TestCase;
import java.io.StringReader;


/**
 * 
 * @author https://github.com/911992
 */
public class MutableJsonReaderTest extends TestCase{

    public void testMutability() throws Exception{
        MutableJsonReader mutableJsonReader = new MutableJsonReader();
        final String keyA = "happy";
        final String keyB = "opensource";
        
        String value = "911";
        String jsonStr=String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}", keyA,value,keyB,value);
        StringReader stringReader = new StringReader(jsonStr);
        mutableJsonReader.reset(stringReader);
        String keyAName,keyBName;
        String keyAValue,keyBValue;
        mutableJsonReader.beginObject();
        keyAName = mutableJsonReader.nextName();
        assertEquals(keyAName, keyA);
        keyAValue = mutableJsonReader.nextString();
        assertEquals(keyAValue, value);
        keyBName = mutableJsonReader.nextName();
        assertEquals(keyBName, keyB);
        keyBValue = mutableJsonReader.nextString();
        assertEquals(keyBValue, value);
        mutableJsonReader.endObject();
        mutableJsonReader.close();
        
        value ="992";
        jsonStr=String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}", keyA,value,keyB,value);
        stringReader = new StringReader(jsonStr);
        mutableJsonReader.reset(stringReader);
        mutableJsonReader.beginObject();
        keyAName = mutableJsonReader.nextName();
        assertEquals(keyAName, keyA);
        keyAValue = mutableJsonReader.nextString();
        assertEquals(keyAValue, value);
        keyBName = mutableJsonReader.nextName();
        assertEquals(keyBName, keyB);
        keyBValue = mutableJsonReader.nextString();
        assertEquals(keyBValue, value);
        mutableJsonReader.endObject();
        mutableJsonReader.close();
    }
    
}
