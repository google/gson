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
package com.google.gson.wsf;

/**
 * Base class for exceptions thrown to indicate a Web service external or application error 
 * condition. This can happen due to bad input, or illegal sequence of operations. This should
 * never be thrown to indicate a System error condition. For that purpose, use 
 * {@link WebServiceSystemException} instead. 
 * 
 * @author inder
 */
public class WebServiceAppException extends RuntimeException {

  private static final long serialVersionUID = 4422041697108937041L;

  public WebServiceAppException(Exception cause) {
    super(cause);
  }
  
  public WebServiceAppException(String msg, Exception cause) {
	super(msg, cause);
  }
}
