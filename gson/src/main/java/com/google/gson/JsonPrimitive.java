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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.JsonPrimitive.CacheStats.HitMissStats;
import com.google.gson.internal.$Gson$Preconditions;
import com.google.gson.internal.LazilyParsedNumber;

/**
 * A class representing a Json primitive value. A primitive value
 * is either a String, a Java primitive, or a Java primitive
 * wrapper type.
 *
 * This class also contains a singleton caches that when enabled will produce immutable JsonPrimitives. Enabling this greatly reduces
 * allocations which result in less GC activity from short lived objects.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class JsonPrimitive extends JsonElement {

  private static final Class<?>[] PRIMITIVE_TYPES = { int.class, long.class, short.class,
      float.class, double.class, byte.class, boolean.class, char.class, Integer.class, Long.class,
      Short.class, Float.class, Double.class, Byte.class, Boolean.class, Character.class };

    private final static JsonPrimitive TRUE = allocBoolean(true);
    private final static JsonPrimitive FALSE = allocBoolean(false);
    private static boolean isCacheEnabled = false;

    private static JsonPrimitive allocBoolean(boolean b) {
        JsonPrimitive primitive = new JsonPrimitive(b);
        primitive.isImmutable = true;
        return primitive;
    }

    /**
     * Enable or disable immutable JsonPrimitive cache
     *
     * Upon disable all caches will be cleared.
     *
     * @param isCacheEnabled turn cache on or off
     */
    public synchronized static void setCacheEnabled(boolean enableCache) {
        if ( enableCache == isCacheEnabled ) {
            // No change
            return;
        }

        if( enableCache ) {
            immutableStringCache = new LRUCache<>(stringCacheSize);
            immutableNumberCache = new LRUCache<>(numberCacheSize);
        }

        isCacheEnabled = enableCache;

        if ( !enableCache ) {
            immutableStringCache = null;
            immutableNumberCache = null;
        }
    }

    /**
     *
     * @return true if cache is active
     */
    public static boolean isCacheEnabled() {
        return isCacheEnabled;
    }

    /**
     * See public static defaults for setup sizes. Caches will be cleared when this method is called.
     *
     * @param stringCacheSize size of string typed JsonPrimitives to cache
     * @param numberCacheSize size of number typed JsonPrimitives to cache
     */
    public static void setCacheSize(int stringCacheSize, int numberCacheSize) {
	JsonPrimitive.stringCacheSize = stringCacheSize;
	JsonPrimitive.numberCacheSize = numberCacheSize;

	if( !isCacheEnabled ){
	    return;
	}

        if (immutableStringCache.getCacheSize() != stringCacheSize) {
            immutableStringCache.setCacheSize(stringCacheSize);
        }
        if (immutableNumberCache.getCacheSize() != numberCacheSize) {
            immutableNumberCache.setCacheSize(numberCacheSize);
        }
    }

    public static class CacheSizes {
	public int stringCacheSize;
	public int numberCacheSize;
    }

    /**
     *
     * @return sizes of each of the caches
     */
    public static CacheSizes getCacheSizes() {
	CacheSizes cacheSizes = new CacheSizes();
	if( !isCacheEnabled ){
	    return cacheSizes;
	}
	cacheSizes.stringCacheSize = immutableStringCache.getCacheSize();
	cacheSizes.numberCacheSize = immutableNumberCache.getCacheSize();

	return cacheSizes;
    }

    /**
     * Force clearing of any cached JsonPrimitive instances
     */
    public static void clearCaches() {
	if( !isCacheEnabled) {
	    return;
	}

        immutableStringCache.clear();
        immutableNumberCache.clear();
    }

    @SuppressWarnings("serial")
    private static final class LRUCache<K> extends LinkedHashMap<K, JsonPrimitive> {
	    private int maxCacheSize;
	    private AtomicLong cacheHit = new AtomicLong(0);
	    private AtomicLong cacheMiss = new AtomicLong(0);

	    public LRUCache(int maxCacheSize) {
	        super(16, (float) 0.75, true);
	        this.maxCacheSize = maxCacheSize;
	    }

	    public synchronized JsonPrimitive allocJsonImmutablePrimitive(K value) {
		$Gson$Preconditions.checkArgument( value != null );
		JsonPrimitive primitive = get(value);
	        if (primitive != null) {
	            cacheHit.incrementAndGet();
	            return primitive;
	        }

	        cacheMiss.incrementAndGet();
	        primitive = new JsonPrimitive(value);
	        primitive.isImmutable = true;
	        put(value, primitive);

	        return primitive;
	    }

	    public long getCacheMissCount() {
		return cacheMiss.get();
	    }

	    public long getCacheHitCount() {
		return cacheHit.get();
	    }

	    /**
	     * Change the cache size limit and clear existing cache
	     *
	     * @param cacheSize >0 will cache that many items, <=0 will cache no items.
	     */
	    public void setCacheSize(int maxCacheSize) {
	        this.maxCacheSize = maxCacheSize;
	        clear();
	    }

	    /**
	     * Returns maximum size of cache
	     *
	     * @return
	     */
	    public int getCacheSize() {
	        return this.maxCacheSize;
	    }

	    @Override
	    protected boolean removeEldestEntry(Map.Entry<K, JsonPrimitive> eldest) {
	        return size() >= maxCacheSize;
	    }

	    @Override
	    public synchronized void clear() {
		super.clear();
		cacheHit.set(0);
		cacheMiss.set(0);
	    }
	};

    public final static int DEFAULT_MAX_CACHED_STRING_SIZE = 512;
    public final static int DEFAULT_STRING_CACHE_SIZE = 5_000; // Max of a 2.5MB cache by default
    public final static int DEFAULT_NUMBER_CACHE_SIZE = 5_000;
    private static int maxCachedStringSize = DEFAULT_MAX_CACHED_STRING_SIZE;
    private static int stringCacheSize = DEFAULT_STRING_CACHE_SIZE;
    private static int numberCacheSize = DEFAULT_NUMBER_CACHE_SIZE;

    private static LRUCache<String> immutableStringCache = null;
    private static LRUCache<Number> immutableNumberCache = null;

    public static class CacheStats {
	public static class HitMissStats {
	    public HitMissStats(long hits, long misses) {
		this.hits = hits;
		this.misses = misses;
	    }
	    public long hits;
	    public long misses;
	}
	HitMissStats string;
	HitMissStats number;
	HitMissStats object;
    }

    /**
     *
     * @return hit and miss statistics of cached objects
     */
    public static CacheStats getCacheStats() {
	CacheStats cacheStats = new CacheStats();
	if( isCacheEnabled ) {
		cacheStats.string = new HitMissStats(immutableStringCache.getCacheHitCount(), immutableStringCache.getCacheMissCount());
		cacheStats.number = new HitMissStats(immutableNumberCache.getCacheHitCount(), immutableNumberCache.getCacheMissCount());
	} else {
		cacheStats.string = new HitMissStats(0, 0);
		cacheStats.number = new HitMissStats(0, 0);
	}

	return cacheStats;
    }
	
    /**
     * All strings greater than this size will not be allowed in the cache.
     *
     * Calling this method will flush the existing strings in the cache.
     * @param maxStringSize max string size allowed in the cache
     */
    public static void setMaxStringCacheSize(int maxStringSize) {
	JsonPrimitive.maxCachedStringSize = maxStringSize;
	immutableStringCache.clear();
    }

    /**
     * If JsonPrimitive caching is enabled.
     *
     * @return size of the largest string allowed to be cached
     */
    public static int getMaxStringCacheSize() {
	return maxCachedStringSize;
    }

    /**
     * Create or use existing from cache immutable JsonPrimitive of type string
     *
     * @param string value of JsonPrimitive
     * @return immutable JsonPrimitive or JsonNull
     */
    public static JsonElement allocate(String string) {
	if (string == null) {
            return JsonNull.INSTANCE;
        }
        if (isCacheEnabled && string.length() <= maxCachedStringSize) {
            return immutableStringCache.allocJsonImmutablePrimitive(string);
        }
        return new JsonPrimitive(string);
    }

    /**
     * Create or use existing from cache immutable JsonPrimitive of type string
     *
     * @param character value of JsonPrimitive
     * @return immutable JsonPrimitive or JsonNull
     */
    public static JsonElement allocate(Character character) {
        if (character == null) {
            return JsonNull.INSTANCE;
        }
        if (isCacheEnabled) {
            return immutableStringCache.allocJsonImmutablePrimitive(String.valueOf(character));
        }
        return new JsonPrimitive(character);
    }

    /**
     * Create or use existing from cache immutable JsonPrimitive of type Number
     *
     * @param number value of JsonPrimitive
     * @return immutable JsonPrimitive or JsonNull
     */
    public static JsonElement allocate(Number number) {
        if (number == null) {
            return JsonNull.INSTANCE;
        }
        if (isCacheEnabled) {
            return immutableNumberCache.allocJsonImmutablePrimitive(number);
        }
        return new JsonPrimitive(number);
    }

    /**
     * Allocate an immutable JsonPrimitive that may be pulled from the cache.
     *
     * @param bool
     * @return immutable JsonPrimitive or JsonNull
     */
    public static JsonElement allocate(Boolean bool) {
        if (bool == null) {
            return JsonNull.INSTANCE;
        }
        if (isCacheEnabled) {
            if (Boolean.TRUE.equals(bool)) {
                return TRUE;
            }
            return FALSE;
        }
        return new JsonPrimitive(bool);
    }

    private Object value;
    private boolean isImmutable = false;

    /**
     * Create a primitive containing a boolean value.
     *
     * @param bool the value to create the primitive with.
     */
    public JsonPrimitive(Boolean bool) {
        setValue(bool);
    }

    /**
     * Create a primitive containing a {@link Number}.
     *
     * @param number the value to create the primitive with.
     */
    public JsonPrimitive(Number number) {
        setValue(number);
    }

    /**
     * Create a primitive containing a String value.
     *
     * @param string the value to create the primitive with.
     */
    public JsonPrimitive(String string) {
        setValue(string);
    }

    /**
     * Create a primitive containing a character. The character is turned into a one character String
     * since Json only supports String.
     *
     * @param c the value to create the primitive with.
     */
    public JsonPrimitive(Character c) {
        setValue(c);
    }

    /**
     * Create a primitive using the specified Object. It must be an instance of {@link Number}, a
     * Java primitive type, or a String.
     *
     * @param primitive the value to create the primitive with.
     */
    JsonPrimitive(Object primitive) {
        setValue(primitive);
    }

    /**
     *
     * @return true if this object may not have the value modified.
     */
    boolean isImmutable() {
        return this.isImmutable;
    }

    @Override
    JsonPrimitive deepCopy() {
        return this;
    }

    void setValue(Object primitive) {
        if (this.isImmutable) {
            throw new IllegalStateException("JsonPrimitive is immutable, may not be modified.");
        }

        if (primitive instanceof Character) {
            // convert characters to strings since in JSON, characters are represented as a single
            // character string
            char c = ((Character) primitive).charValue();
            this.value = String.valueOf(c);
        } else {
            $Gson$Preconditions.checkArgument(primitive instanceof Number
                    || isPrimitiveOrString(primitive));
            this.value = primitive;
        }
    }

    /**
     * Check whether this primitive contains a boolean value.
     *
     * @return true if this primitive contains a boolean value, false otherwise.
     */
    public boolean isBoolean() {
        return value instanceof Boolean;
    }

    /**
     * convenience method to get this element as a {@link Boolean}.
     *
     * @return get this element as a {@link Boolean}.
     */
    @Override
    Boolean getAsBooleanWrapper() {
        return (Boolean) value;
    }

    /**
     * convenience method to get this element as a boolean value.
     *
     * @return get this element as a primitive boolean value.
     */
    @Override
    public boolean getAsBoolean() {
        if (isBoolean()) {
            return getAsBooleanWrapper().booleanValue();
        } else {
            // Check to see if the value as a String is "true" in any case.
            return Boolean.parseBoolean(getAsString());
        }
    }

    /**
     * Check whether this primitive contains a Number.
     *
     * @return true if this primitive contains a Number, false otherwise.
     */
    public boolean isNumber() {
        return value instanceof Number;
    }

    /**
     * convenience method to get this element as a Number.
     *
     * @return get this element as a Number.
     * @throws NumberFormatException if the value contained is not a valid Number.
     */
    @Override
    public Number getAsNumber() {
        return value instanceof String ? new LazilyParsedNumber((String) value) : (Number) value;
    }

    /**
     * Check whether this primitive contains a String value.
     *
     * @return true if this primitive contains a String value, false otherwise.
     */
    public boolean isString() {
        return value instanceof String;
    }

    /**
     * convenience method to get this element as a String.
     *
     * @return get this element as a String.
     */
    @Override
    public String getAsString() {
        if (isNumber()) {
            return getAsNumber().toString();
        } else if (isBoolean()) {
            return getAsBooleanWrapper().toString();
        } else {
            return (String) value;
        }
    }

    /**
     * convenience method to get this element as a primitive double.
     *
     * @return get this element as a primitive double.
     * @throws NumberFormatException if the value contained is not a valid double.
     */
    @Override
    public double getAsDouble() {
        return isNumber() ? getAsNumber().doubleValue() : Double.parseDouble(getAsString());
    }

    /**
     * convenience method to get this element as a {@link BigDecimal}.
     *
     * @return get this element as a {@link BigDecimal}.
     * @throws NumberFormatException if the value contained is not a valid {@link BigDecimal}.
     */
    @Override
    public BigDecimal getAsBigDecimal() {
        return value instanceof BigDecimal ? 
        	(BigDecimal) value : new BigDecimal(value.toString());
    }

    /**
     * convenience method to get this element as a {@link BigInteger}.
     *
     * @return get this element as a {@link BigInteger}.
     * @throws NumberFormatException if the value contained is not a valid {@link BigInteger}.
     */
    @Override
    public BigInteger getAsBigInteger() {
        return value instanceof BigInteger ?
        	(BigInteger) value : new BigInteger(value.toString());
    }

    /**
     * convenience method to get this element as a float.
     *
     * @return get this element as a float.
     * @throws NumberFormatException if the value contained is not a valid float.
     */
    @Override
    public float getAsFloat() {
        return isNumber() ? getAsNumber().floatValue() : Float.parseFloat(getAsString());
    }

    /**
     * convenience method to get this element as a primitive long.
     *
     * @return get this element as a primitive long.
     * @throws NumberFormatException if the value contained is not a valid long.
     */
    @Override
    public long getAsLong() {
        return isNumber() ? getAsNumber().longValue() : Long.parseLong(getAsString());
    }

    /**
     * convenience method to get this element as a primitive short.
     *
     * @return get this element as a primitive short.
     * @throws NumberFormatException if the value contained is not a valid short value.
     */
    @Override
    public short getAsShort() {
        return isNumber() ? getAsNumber().shortValue() : Short.parseShort(getAsString());
    }

    /**
     * convenience method to get this element as a primitive integer.
     *
     * @return get this element as a primitive integer.
     * @throws NumberFormatException if the value contained is not a valid integer.
     */
    @Override
    public int getAsInt() {
        return isNumber() ? getAsNumber().intValue() : Integer.parseInt(getAsString());
    }

    @Override
    public byte getAsByte() {
        return isNumber() ? getAsNumber().byteValue() : Byte.parseByte(getAsString());
    }

    @Override
    public char getAsCharacter() {
        return getAsString().charAt(0);
    }

    private static boolean isPrimitiveOrString(Object target) {
        if (target instanceof String) {
            return true;
        }

        Class<?> classOfPrimitive = target.getClass();
        for (Class<?> standardPrimitive : PRIMITIVE_TYPES) {
            if (standardPrimitive.isAssignableFrom(classOfPrimitive)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (value == null) {
            return 31;
        }
        // Using recommended hashing algorithm from Effective Java for longs and doubles
        if (isIntegral(this)) {
            long value = getAsNumber().longValue();
            return (int) (value ^ (value >>> 32));
        }
        if (value instanceof Number) {
            long value = Double.doubleToLongBits(getAsNumber().doubleValue());
            return (int) (value ^ (value >>> 32));
        }
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        JsonPrimitive other = (JsonPrimitive)obj;
        if (value == null) {
            return other.value == null;
        }
        if (isIntegral(this) && isIntegral(other)) {
            return getAsNumber().longValue() == other.getAsNumber().longValue();
        }
        if (value instanceof Number && other.value instanceof Number) {
            double a = getAsNumber().doubleValue();
            // Java standard types other than double return true for two NaN. So, need
            // special handling for double.
            double b = other.getAsNumber().doubleValue();
            return a == b || (Double.isNaN(a) && Double.isNaN(b));
        }
        return value.equals(other.value);
    }

    /**
     * Returns true if the specified number is an integral type
     * (Long, Integer, Short, Byte, BigInteger)
     */
    private static boolean isIntegral(JsonPrimitive primitive) {
        if (primitive.value instanceof Number) {
            Number number = (Number) primitive.value;
            return number instanceof BigInteger || number instanceof Long || number instanceof Integer
                    || number instanceof Short || number instanceof Byte;
        }
        return false;
    }
}
