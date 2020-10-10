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
File: MutableJsonWriterTest.java
Created on: Oct 10, 2020 12:41:40 PM
    @author https://github.com/911992
 
History:
    initial version: 0.1(20201010)
*/

package com.google.gson.stream;

import java.io.StringWriter;
import junit.framework.TestCase;

/**
 * 
 * @author https://github.com/911992
 */
public class MutableJsonWriterTest extends TestCase{
    
    public void testMutability() throws Exception{
        MutableJsonWriter _shared_writer = new MutableJsonWriter();
        final String _a = "happy";
        final String _b = "opensource";
        
        String _val = "911";
        String _expected_json=String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}", _a,_val,_b,_val);
        StringWriter _str_writer = new StringWriter();
        _shared_writer.reset(_str_writer);
        _shared_writer.beginObject();
        _shared_writer.name(_a);
        _shared_writer.value(_val);
        _shared_writer.name(_b);
        _shared_writer.value(_val);
        _shared_writer.endObject();
        _shared_writer.flush();
        _shared_writer.close();
        String _gen_json = _str_writer.toString();
//        System.out.printf("gen json: %s\n",_gen_json);
        assertEquals(_expected_json, _gen_json);
        
        _val = "992";
        _expected_json=String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}", _a,_val,_b,_val);
        _str_writer = new StringWriter();
        _shared_writer.reset(_str_writer);
        _shared_writer.beginObject();
        _shared_writer.name(_a);
        _shared_writer.value(_val);
        _shared_writer.name(_b);
        _shared_writer.value(_val);
        _shared_writer.endObject();
        _shared_writer.flush();
        _shared_writer.close();
        _gen_json = _str_writer.toString();
//        System.out.printf("gen json: %s\n",_gen_json);
        assertEquals(_expected_json, _gen_json);
    }
    
}
