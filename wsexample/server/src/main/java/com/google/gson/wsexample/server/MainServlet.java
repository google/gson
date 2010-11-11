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
package com.google.gson.wsexample.server;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.webservice.definition.CallPath;

/**
 * An example servlet that receives JSON web-service requests
 *
 * @author inder
 */
@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {
  private final RestDispatcher restDispatcher;
  private final ProcedureDispatcher procedureDispatcher;

  public MainServlet() {
    this.restDispatcher = new RestDispatcher();
    this.procedureDispatcher = new ProcedureDispatcher();
  }

  @Override
  public void service(HttpServletRequest req, HttpServletResponse res) {
    String servletPath = req.getServletPath();
    int index = "/wsexampleserver".length();
    CallPath callPath = new CallPath(servletPath.substring(index));
    String path = callPath.get();
    if (path.startsWith("/rest")) {
      restDispatcher.service(req, res, callPath);
    } else if (path.startsWith("/procedure")) {
      procedureDispatcher.service(req, res);
    }
  }
}
