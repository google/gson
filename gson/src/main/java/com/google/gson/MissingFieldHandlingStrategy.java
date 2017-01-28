package com.google.gson;

import com.google.gson.reflect.TypeToken;

/**
 * @author Prateek Jain
 */
public interface MissingFieldHandlingStrategy {
    Object handle(TypeToken type, String fieldName);
}
