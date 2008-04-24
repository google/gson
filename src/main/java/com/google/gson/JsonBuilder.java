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

package com.google.gson;

/**
 * This interface represents a means to serialize objects
 * to Json format. Once you get a JsonWriter, you can add 
 * objects and arrays to it. To add an object to the JsonWriter
 *   
 * @author Inderjeet Singh
 */
public interface JsonBuilder {
  
  public interface ObjectBuilder {
    /**
     * Adds a scalar property to the object.
     *  
     * @param key The key under which the property is being added
     * @param value The value for the property. The toString() method
     *        of the specified object is used as the scalar value. 
     */
    public void addScalar(String key, Object value);
    
    /**
     * Adds an object property with the specified key.
     *  
     * @return an {@link ObjectBuilder} that can be used to 
     *         build the contents of the new object 
     */    
    public ObjectBuilder addObject(String key);
    
    /**
     * Adds an array property with the specified key.
     *  
     * @return an {@link ArrayBuilder} that can be used to 
     *         build the contents of the new array
     */    
    public ArrayBuilder addArray(String key);

    /**
     * finishes the building of this object. Any subsequent calls
     * to build this object further is an error. 
     */    
    public void finish();
  }

  public interface ArrayBuilder {
    /**
     * Adds a scalar element to the array.
     *  
     * @param value The value of the element. The toString() method
     *        of the specified object is used as the scalar value. 
     */
    public void addScalar(Object value);
    
    /**
     * Adds an object element to the array.
     *  
     * @return an {@link ObjectBuilder} that can be used to 
     *         build the contents of the new object 
     */    
    public ObjectBuilder addObject();
    
    /**
     * Adds an array element to the array. 
     *  
     * @return an {@link ArrayBuilder} that can be used to 
     *         build the contents of the new array
     */    
    public ArrayBuilder addArray();
    
    /**
     * finishes the building of this object. Any subsequent calls
     * to build this object further is an error. 
     */    
    public void finish();
  }
  
  /**
   * Configures this builder to contain an object value. 
   * Any subsequent calls to {@link #makeObject}, 
   * {@link #makeArray} or {@link #makeScalar} is not allowed. 
   *  
   * @return an {@link ObjectBuilder} that can be used to 
   *         build the Json as an object 
   */
  public ObjectBuilder makeObject();
  
  /**
   * Configures this builder to contain an array value. 
   * Any subsequent calls to {@link #makeObject}, 
   * {@link #makeArray} or {@link #makeScalar} is not allowed. 
   *  
   * @return an {@link ArrayBuilder} that can be used to 
   *         build the Json as an array
   */
  public ArrayBuilder makeArray();

  /**
   * Makes this builder to be a scalar value. A scalar value 
   * is either primitive value or a string.  
   * Any subsequent calls to {@link #makeObject}, 
   * {@link #makeArray} or {@link #makeScalar} is not allowed.
   * If this is a top-level JSON object, then the caller is required
   * to ensure that it is wrapped in a single-element array. 
   * 
   * @param value the scalar value to which this builder
   *        is set. The toString() method of the specified
   *        object is used as the scalar value. 
   */
  public void makeScalar(Object value);   
}
