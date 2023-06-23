package com.example;

import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;

/**
 * Uses {@link Since} and {@link Until} annotations.
 */
public class ClassWithVersionAnnotations {
  @Since(1)
  int i1;

  @Until(1) // will be ignored with GsonBuilder.setVersion(1)
  int i2;

  @Since(2) // will be ignored with GsonBuilder.setVersion(1)
  int i3;

  @Until(2)
  int i4;
}
