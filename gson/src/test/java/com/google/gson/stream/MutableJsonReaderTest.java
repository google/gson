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
        MutableJsonReader _shared_reader = new MutableJsonReader();
        final String _a = "happy";
        final String _b = "opensource";
        
        String _val = "911";
        String _json=String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}", _a,_val,_b,_val);
        StringReader _str_reader = new StringReader(_json);
        _shared_reader.reset(_str_reader);
        String _a_name,_b_name;
        String _a_val,_b_val;
        _shared_reader.beginObject();
        _a_name = _shared_reader.nextName();
        assertEquals(_a_name, _a);
        _a_val = _shared_reader.nextString();
        assertEquals(_a_val, _val);
        _b_name = _shared_reader.nextName();
        assertEquals(_b_name, _b);
        _b_val = _shared_reader.nextString();
        assertEquals(_b_val, _val);
        _shared_reader.endObject();
        _shared_reader.close();
        
        _val ="992";
        _json=String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}", _a,_val,_b,_val);
        _str_reader = new StringReader(_json);
        _shared_reader.reset(_str_reader);
        _shared_reader.beginObject();
        _a_name = _shared_reader.nextName();
        assertEquals(_a_name, _a);
        _a_val = _shared_reader.nextString();
        assertEquals(_a_val, _val);
        _b_name = _shared_reader.nextName();
        assertEquals(_b_name, _b);
        _b_val = _shared_reader.nextString();
        assertEquals(_b_val, _val);
        _shared_reader.endObject();
        _shared_reader.close();
    }
    
}
