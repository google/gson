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

/**
 * Base class for all exceptions thrown by the Web service to indicate a system error condition. 
 * This should never be thrown to indicate bad user input.
 *
 * @author inder
 */
public class WebServiceSystemException extends RuntimeException {

  private static final long serialVersionUID = -2511829073381716183L;

  public WebServiceSystemException(Exception cause) {
    super(cause);
  }
  
  public WebServiceSystemException(String msg, Exception cause) {
	  super(msg, cause);
  }
}
