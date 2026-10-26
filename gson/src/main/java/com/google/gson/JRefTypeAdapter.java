package com.google.gson;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;

public class JRefTypeAdapter extends TypeAdapter<Object> {

	public static final String JREF_NAME = "$ref";
	public static final String HASH = "#";

	class JRefTokenStreamContext extends TokenStreamContext {

		private JRefTokenStreamContext parent;
		private String currentName;

		// Arrays for serialization (known length)
		JRefTokenStreamContext(JRefTokenStreamContext parent, int type, int index) {
			this.parent = parent;
			this._type = type;
			this._index = index;
		}

		// Arrays
		JRefTokenStreamContext(JRefTokenStreamContext parent, int index) {
			this(parent, TYPE_ARRAY, index);
		}

		// Objects
		JRefTokenStreamContext(JRefTokenStreamContext parent, String name) {
			this(parent, TYPE_OBJECT, -1);
			this.currentName = name;
		}

		boolean isKey = false;

		// For map keys
		JRefTokenStreamContext(JRefTokenStreamContext parent, String name, boolean isKey) {
			this(parent, TYPE_OBJECT, -1);
			this.currentName = name;
			this.isKey = true;
		}

		boolean isKey() {
			return isKey;
		}

		@Override
		public TokenStreamContext getParent() {
			return parent;
		}

		@Override
		public String currentName() {
			return currentName;
		}

		@Override
		public void assignCurrentValue(Object v) {
		}

	}

	public static ThreadLocal<Map<JsonPointer, Object>> ptrToValue = ThreadLocal.withInitial(() -> new HashMap<>());
	public static ThreadLocal<Map<Object, JsonPointer>> valueToPtr = ThreadLocal.withInitial(() -> new HashMap<>());
	public static ThreadLocal<JRefTokenStreamContext> readTSC = new ThreadLocal<>();
	public static ThreadLocal<JRefTokenStreamContext> writeTSC = new ThreadLocal<>();

	private final TypeToken<Object> type;
	private final TypeAdapter<Object> delegate;

	JRefTypeAdapter(TypeToken<Object> type, TypeAdapter<Object> delegateTypeAdapter) {
		this.type = type;
		this.delegate = delegateTypeAdapter;
	}

	JRefTokenStreamContext createTSC(JRefTokenStreamContext parent) {
		if (type.getRawType().isArray() || Collection.class.isAssignableFrom(type.getRawType())) {
			return new JRefTokenStreamContext(parent, TokenStreamContext.TYPE_ARRAY, 0);
		} else {
			return new JRefTokenStreamContext(parent, null);
		}
	}

	@Override
	public void write(JsonWriter out, Object value) throws IOException {
		try {
			// get previousTSC
			JRefTokenStreamContext previousTSC = writeTSC.get();
			String deferredName = out.getDeferredName();
			if (previousTSC != null && deferredName != null) {
				previousTSC = new JRefTokenStreamContext(previousTSC.parent, deferredName);
			}
			// Create our context using the (potentially modified) parent context from above
			writeTSC.set(createTSC(previousTSC));
			// check if value has a ptr already associated with it
			JsonPointer valuePtr = valueToPtr.get().get(value);
			if (valuePtr != null) {
				// If found write jref and done
				out.beginObject();
				out.name(JREF_NAME);
				out.value("#" + valuePtr.toString());
				out.endObject();
			} else {
				// Call the delegate to do the actual serialization
				this.delegate.write(out, value);
			}
			// get parent context
			JRefTokenStreamContext parentTSC = writeTSC.get().parent;
			// and get current valueToPtrMap
			Map<Object, JsonPointer> valueToPtrMap = valueToPtr.get();
			// parentTSC will be null for root
			if (parentTSC != null) {
				// Here is where the serialized value is assciated with a ptr
				if (value != null && !valueToPtrMap.containsKey(value)) {
					JsonPointer ptr = parentTSC.pathAsPointer();
					valueToPtrMap.put(value, ptr);
				}
				// If an array element we just set, then increment index
				if (parentTSC.inArray()) {
					parentTSC = new JRefTokenStreamContext(parentTSC.parent, TokenStreamContext.TYPE_ARRAY,
							++parentTSC._index);
				}
			} else {
				// we are done so free up valueToPtrMap contents
				valueToPtrMap.clear();
			}
			// Finished with this write, so set to parentTSC
			writeTSC.set(parentTSC);
		} catch (IOException e) {
			// clear the static valueToPtr
			valueToPtr.get().clear();
			throw e;
		}
	}

	@Override
	public Object read(JsonReader in) throws IOException {
		try {
			JRefTokenStreamContext previousTSC = readTSC.get();
			if (previousTSC != null) {
				// Get ptr for previousTSC
				JsonPointer parentPtr = JsonPointer.forPath(previousTSC, false);
				// Convert jsonPath...e.g. '$..name[0] to jsonPointerPath...e.g. /name/0
				String jsonPointerPath = "/" + getJsonPointerFromJsonPath(in.getPath());
				// Use it to get ctxtPtr
				JsonPointer ctxtPtr = JsonPointer.valueOf(jsonPointerPath);
				String newParentName = null;
				// If parentPtr is same as ctxtPtr, then no newParentName
				if (parentPtr.equals(ctxtPtr)) {
					// If parent and ctxt same, then no new parent name
					newParentName = null;
				} else {
					String ctxtPtrStr = ctxtPtr.last().toString().substring(1);
					// If parentPtr not set/empty then we set newParentName
					if (parentPtr.equals(JsonPointer.EMPTY)) {
						newParentName = ctxtPtrStr;
					} else {
						// If context is empty
						if ("".equals(ctxtPtrStr)) {
							// if we are not a key(
							if (!previousTSC.isKey()) {
								// newParentName is empty string
								newParentName = "";
							} else {
								// No newParentName
								newParentName = null;
							}
						} else {
							// typical case for new parent name
							newParentName = ctxtPtrStr;
						}
					}
				}
				if (newParentName != null) {
					if (previousTSC.inArray()) {
						// newParentName is array index
						previousTSC = new JRefTokenStreamContext(previousTSC.parent, Integer.valueOf(newParentName));
					} else {
						// newParentName is object name
						previousTSC = new JRefTokenStreamContext(previousTSC.parent, newParentName);
					}
				}
			}
			readTSC.set(createTSC(previousTSC));
			// Here is where reading starts
			Object result = null;
			// Look for begin object
			if (in.peek() == JsonToken.BEGIN_OBJECT) {
				JsonElement e = Streams.parse(in);
				JsonElement r = ((JsonObject) e).get(JREF_NAME);
				if (r != null) {
					// remove # (local only jrefs)
					String jrefValue = r.getAsString().substring(1);
					// create JsonPointer from jrefValue
					JsonPointer ptr = JsonPointer.valueOf(jrefValue);
					if (JsonPointer.EMPTY.equals(ptr)) {
						throw new MalformedJsonException(
								String.format("Empty JsonPointer for jrefValue=%s", jrefValue));
					}
					result = ptrToValue.get().get(ptr);
					if (result == null) {
						throw new MalformedJsonException(
								String.format("Could not lookup value for JsonPointer=%s", ptr));
					}
				}
				if (result == null) {
					result = this.delegate.fromJsonTree(e);
				}
			}
			if (result == null) {
				result = this.delegate.read(in);
			}
			// unwinde tokenstreamcontext
			JRefTokenStreamContext currTSC = readTSC.get();
			JRefTokenStreamContext parentTSC = currTSC.parent;
			Map<JsonPointer, Object> resultsMap = (Map<JsonPointer, Object>) ptrToValue.get();
			if (parentTSC != null) {
				// "" name means that we set the parentTSC with result (map key)
				if ("".equals(parentTSC.currentName())) {
					parentTSC = new JRefTokenStreamContext(parentTSC.parent, (String) result, true);
				} else {
					if (result != null) {
						JsonPointer ptr = parentTSC.pathAsPointer();
						// put ptr into map if not already there
						if (!resultsMap.containsKey(ptr)) {
							resultsMap.put(ptr, result);
						}
						// If this is a key that was just set, reset with name = ""
						if (parentTSC.isKey()) {
							parentTSC = new JRefTokenStreamContext(parentTSC.parent, "");
						}
					}
				}
			} else {
				// no parentTSC, so we are done. Clear static map.
				resultsMap.clear();
			}
			readTSC.set(parentTSC);
			return result;
		} catch (IOException e) {
			// Clear out static map if exception
			ptrToValue.get().clear();
			// rethrow
			throw e;
		}
	}

	private String getJsonPointerFromJsonPath(String jsonPath) throws IOException {
		if (jsonPath.startsWith("$") || jsonPath.startsWith(".")) {
			// remove/return '$' and '.' from jsonPath
			return getJsonPointerFromJsonPath(jsonPath.substring(1));
		} else {
			StringBuffer buf = new StringBuffer();
			int leftBracketLoc = jsonPath.indexOf('[');
			//
			if (leftBracketLoc == -1) {
				// no left bracket/arrays at all...simply return
				return jsonPath;
			} else if (leftBracketLoc > 0) {
				// append name to buffer
				buf.append(jsonPath.substring(0, leftBracketLoc));
				// get remainder
				jsonPath = jsonPath.substring(leftBracketLoc);
				if (!jsonPath.isEmpty()) {
					buf.append("/").append(getJsonPointerFromJsonPath(jsonPath));
				}
			} else {
				// close bracket expected
				int closeBracketLoc = jsonPath.indexOf(']');
				if (closeBracketLoc > -1) {
					buf.append(jsonPath.substring(leftBracketLoc + 1, closeBracketLoc)).toString();
					jsonPath = jsonPath.substring(closeBracketLoc + 1);
					if (!jsonPath.isEmpty()) {
						buf.append("/");
						buf.append(getJsonPointerFromJsonPath(jsonPath));
					}
				} else {
					throw new IOException(
							String.format("Bad index value starting with leftBracketLoc=%s in jsonPath=%s",
									leftBracketLoc, jsonPath));
				}
			}
			return buf.toString();
		}
	}

}
