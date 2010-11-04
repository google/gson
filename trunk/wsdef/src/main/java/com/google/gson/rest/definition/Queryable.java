// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.gson.rest.definition;

/**
 * Implement this interface in a service to indicate that it allows querying
 * by this type
 * 
 * @author Inderjeet Singh
 */
public interface Queryable<QUERY, RESULTS> {
  public RESULTS query(QUERY query);
}
