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
package com.google.gson.webservice.definition.procedural;

import com.google.gson.webservice.definition.HeaderMapSpec;
import com.google.gson.webservice.definition.internal.utils.Preconditions;

/**
 * Specification for a {@link WebServiceRequest}.
 * 
 * @author inder
 */
public final class RequestSpec {

  private final HeaderMapSpec headersSpec;
  private final RequestBodySpec bodySpec;
  
  public RequestSpec(HeaderMapSpec headersSpec, RequestBodySpec bodySpec) {
    Preconditions.checkNotNull(headersSpec);
    Preconditions.checkNotNull(bodySpec);
    
    this.headersSpec = headersSpec;
    this.bodySpec = bodySpec;
  }
  
  public HeaderMapSpec getHeadersSpec() {
    return headersSpec;
  }
 
  public RequestBodySpec getBodySpec() {
    return bodySpec;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("{headersSpec:");
    sb.append(headersSpec).append(",bodySpec:");
    sb.append(bodySpec).append("}");
    return sb.toString();
  }
}
