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

import com.google.common.collect.Lists;
import com.google.gson.reflect.DisjunctionExclusionStrategy;
import com.google.gson.reflect.ExclusionStrategy;
import com.google.gson.reflect.InnerClassExclusionStrategy;
import com.google.gson.reflect.ModifierBasedExclusionStrategy;
import com.google.gson.reflect.ObjectNavigatorFactory;
import com.google.gson.version.VersionConstants;
import com.google.gson.version.VersionExclusionStrategy;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Use this builder to construct a Gson instance in situations where
 * you need to set a number of parameters. 
 * 
 * @author Inderjeet Singh
 */
public final class GsonBuilder {
  
  private double ignoreVersionsAfter;
  private ModifierBasedExclusionStrategy modifierBasedExclusionStrategy;
  private InnerClassExclusionStrategy innerClassExclusionStrategy;
  private TypeAdapter typeAdapter;
  
  public GsonBuilder() {
    // setup default values    
    ignoreVersionsAfter = VersionConstants.IGNORE_VERSIONS;    
    innerClassExclusionStrategy = new InnerClassExclusionStrategy();    
    modifierBasedExclusionStrategy = Gson.DEFAULT_MODIFIER_BASED_EXCLUSION_STRATEGY;    
    typeAdapter = Gson.DEFAULT_TYPE_ADAPTER;
  }
  
  /**
   * Use this setter to enable versioning support. 
   * @param ignoreVersionsAfter any field or type marked with a
   *        version higher than this value are ignored during serialization
   *        or deserialization.
   */
  public GsonBuilder setVersion(double ignoreVersionsAfter) {
    this.ignoreVersionsAfter = ignoreVersionsAfter;
    return this;
  }
  
  /**
   * Setup Gson such that it excludes all class fields that have
   * the specified modifiers. By default, Gson will exclude all fields
   * marked transient or static. This method will override that behavior. 
   *  
   * @param modifiers the field modifiers. You must use the modifiers
   *        specified in the {@link Modifier} class. For example, 
   *        {@link Modifier#TRANSIENT}, {@link Modifier#STATIC}
   */
  public GsonBuilder excludeFieldsWithModifiers(int... modifiers) {
    boolean skipSynthetics = true;
    modifierBasedExclusionStrategy = new ModifierBasedExclusionStrategy(skipSynthetics, modifiers);
    return this;    
  }
  
  /**
   * @return an instance of Gson configured with the parameters set 
   *         in this builder
   */
  public Gson create() {
    List<ExclusionStrategy> strategies = Lists.newArrayList(
        innerClassExclusionStrategy,
        modifierBasedExclusionStrategy);
    if (ignoreVersionsAfter != VersionConstants.IGNORE_VERSIONS) {
      strategies.add(new VersionExclusionStrategy(ignoreVersionsAfter));
    }
    ExclusionStrategy exclusionStrategy = new DisjunctionExclusionStrategy(strategies);
    ObjectNavigatorFactory objectNavigatorFactory = new ObjectNavigatorFactory(exclusionStrategy);
    MappedObjectConstructor objectConstructor = new MappedObjectConstructor();
    return new Gson(objectNavigatorFactory, objectConstructor, typeAdapter);
  }
}
