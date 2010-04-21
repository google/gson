/*
 * Copyright (C) 2008 Google Inc.
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
package com.google.gson.webservice.definition;

import java.util.Map;

/**
 * Body of a request or response. The body contains a map of name-value pairs.
 * There is a {@link ContentBodySpec} associated with the body as well and only the name-value 
 * pairs consistent with the specification are permitted.
 * 
 * @author inder
 */
class ContentBody extends ParamMap {
  
  ContentBody(ContentBodySpec spec, Map<String, Object> contents) {
    super(spec, contents);
  }
  
  @Override
  public ContentBodySpec getSpec() {
    return (ContentBodySpec) spec;
  }

  public String getContentType() {
    return getSpec().getContentType();
  }
  
  public String getCharacterEncoding() {
    return getSpec().getCharacterEncoding();
  }
}
