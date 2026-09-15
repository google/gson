package com.google.gson;

import java.util.Arrays;

import com.google.gson.JsonParser;

/**
 * Shared base class for streaming processing contexts used during reading and
 * writing of token streams using Streaming API. This context is also exposed to
 * applications: context object can be used by applications to get an idea of
 * relative position of the parser/generator within content being processed.
 * This allows for some contextual processing: for example, output within Array
 * context can differ from that of Object context. Perhaps more importantly
 * context is hierarchic so that enclosing contexts can be inspected as well.
 * All levels also include information about current property name (for Objects)
 * and element index (for Arrays).
 * <p>
 * NOTE: in Jackson 2.x this class was named {@code JsonStreamContext}
 */
public abstract class TokenStreamContext {
	// // // Type constants used internally
	// // // (but exposed publicly as of 2.12 as possibly needed)

	/**
	 * Indicator for "Root Value" context (has not parent)
	 */
	public final static int TYPE_ROOT = 0;

	/**
	 * Indicator for "Array" context.
	 */
	public final static int TYPE_ARRAY = 1;

	/**
	 * Indicator for "Object" context.
	 */
	public final static int TYPE_OBJECT = 2;

	/**
	 * Indicates logical type of context as one of {@code TYPE_xxx} constants.
	 */
	protected int _type;

	/**
	 * Index of the currently processed entry. Starts with -1 to signal that no
	 * entries have been started, and gets advanced each time a new entry is
	 * started, either by encountering an expected separator, or with new values if
	 * no separators are expected (the case for root context).
	 */
	protected int _index;

	/**
	 * The nesting depth is a count of objects and arrays that have not been closed,
	 * `{` and `[` respectively.
	 *
	 * @since 2.15
	 */
	protected int _nestingDepth;

	/*
	 * /********************************************************************** /*
	 * Life-cycle
	 * /**********************************************************************
	 */

	protected TokenStreamContext() {
	}

	/**
	 * Copy constructor used by sub-classes for creating copies for buffering.
	 *
	 * @param base Context instance to copy type and index from
	 */
	protected TokenStreamContext(TokenStreamContext base) {
		_type = base._type;
		_index = base._index;
	}

	protected TokenStreamContext(int type, int index) {
		_type = type;
		_index = index;
	}

	/*
	 * /********************************************************************** /*
	 * Public API, accessors
	 * /**********************************************************************
	 */

	/**
	 * Accessor for finding parent context of this context; will return null for
	 * root context.
	 *
	 * @return Parent context of this context, if any; {@code null} for Root
	 *         contexts
	 */
	public abstract TokenStreamContext getParent();

	/**
	 * Method that returns true if this context is an Array context; that is,
	 * content is being read from or written to a JSON Array.
	 *
	 * @return {@code True} if this context represents an Array; {@code false}
	 *         otherwise
	 */
	public final boolean inArray() {
		return _type == TYPE_ARRAY;
	}

	/**
	 * Method that returns true if this context is a Root context; that is, content
	 * is being read from or written to without enclosing array or object structure.
	 *
	 * @return {@code True} if this context represents a sequence of Root values;
	 *         {@code false} otherwise
	 */
	public final boolean inRoot() {
		return _type == TYPE_ROOT;
	}

	/**
	 * Method that returns true if this context is an Object context; that is,
	 * content is being read from or written to a JSON Object.
	 *
	 * @return {@code True} if this context represents an Object; {@code false}
	 *         otherwise
	 */
	public final boolean inObject() {
		return _type == TYPE_OBJECT;
	}

	/**
	 * The nesting depth is a count of objects and arrays that have not been closed,
	 * `{` and `[` respectively.
	 *
	 * @return Nesting depth
	 *
	 * @since 2.15
	 */
	public final int getNestingDepth() {
		return _nestingDepth;
	}

	/**
	 * Method for accessing simple type description of current context; either ROOT
	 * (for root-level values), OBJECT (for Object property names and values) or
	 * ARRAY (for elements of JSON Arrays)
	 *
	 * @return Type description String
	 */
	public String typeDesc() {
		switch (_type) {
		case TYPE_ROOT:
			return "root";
		case TYPE_ARRAY:
			return "Array";
		case TYPE_OBJECT:
			return "Object";
		}
		return "?";
	}

	/**
	 * @return Number of entries that are complete and started.
	 */
	public final int getEntryCount() {
		return _index + 1;
	}

	/**
	 * @return Index of the currently processed entry, if any
	 */
	public final int getCurrentIndex() {
		return (_index < 0) ? 0 : _index;
	}

	/**
	 * Method that may be called to verify whether this context has valid index:
	 * will return `false` before the first entry of Object context or before first
	 * element of Array context; otherwise returns `true`.
	 *
	 * @return {@code True} if this context has value index to access, {@code false}
	 *         otherwise
	 */
	public boolean hasCurrentIndex() {
		return _index >= 0;
	}

	/**
	 * Method that may be called to check if this context is either:
	 * <ul>
	 * <li>Object, with at least one entry written (partially or completely)</li>
	 * <li>Array, with at least one entry written (partially or completely)</li>
	 * </ul>
	 * and if so, return `true`; otherwise return `false`. Latter case includes Root
	 * context (always), and Object/Array contexts before any entries/elements have
	 * been read or written.
	 * <p>
	 * Method is mostly used to determine whether this context should be used for
	 * constructing {@link JsonPointer}
	 *
	 * @return {@code True} if this context has value path segment to access,
	 *         {@code false} otherwise
	 */
	public boolean hasPathSegment() {
		if (_type == TYPE_OBJECT) {
			return hasCurrentName();
		} else if (_type == TYPE_ARRAY) {
			return hasCurrentIndex();
		}
		return false;
	}

	/**
	 * Method for accessing name associated with the current location. Non-null for
	 * <code>PROPERTY_NAME</code> and value events that directly follow Property
	 * names; null for root level and array values.
	 *
	 * @return Current property name within context, if any; {@code null} if none
	 */
	public abstract String currentName();

	public boolean hasCurrentName() {
		return currentName() != null;
	}

	/**
	 * Method for accessing currently active value being used by data-binding (as
	 * the source of streaming data to write, or destination of data being read), at
	 * this level in hierarchy.
	 * <p>
	 * Note that "current value" is NOT populated (or used) by Streaming parser or
	 * generator; it is only used by higher-level data-binding functionality. The
	 * reason it is included here is that it can be stored and accessed
	 * hierarchically, and gets passed through data-binding.
	 *
	 * @return Currently active value, if one has been assigned.
	 */
	public Object currentValue() {
		return null;
	}

	/**
	 * Method to call to pass value to be returned via {@link #currentValue};
	 * typically called indirectly through {@link JsonParser#assignCurrentValue} or
	 * {@link JsonGenerator#assignCurrentValue}).
	 *
	 * @param v Current value to assign to this context
	 */
	public void assignCurrentValue(Object v) {
	}

	/**
	 * Factory method for constructing a {@link JsonPointer} that points to the
	 * current location within the stream that this context is for, excluding
	 * information about "root context" (only relevant for multi-root-value cases)
	 *
	 * @return Pointer instance constructed
	 */
	public JsonPointer pathAsPointer() {
		return JsonPointer.forPath(this, false);
	}

	/**
	 * Factory method for constructing a {@link JsonPointer} that points to the
	 * current location within the stream that this context is for, optionally
	 * including "root value index"
	 *
	 * @param includeRoot Whether root-value offset is included as the first segment
	 *                    or not
	 *
	 * @return Pointer instance constructed
	 */
	public JsonPointer pathAsPointer(boolean includeRoot) {
		return JsonPointer.forPath(this, includeRoot);
	}

	/**
	 * Overridden to provide developer readable "JsonPath" representation of the
	 * context.
	 *
	 * @return Simple developer-readable description this context layer (note: NOT
	 *         constructed with parents, unlike {@link #pathAsPointer})
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		switch (_type) {
		case TYPE_ROOT:
			sb.append("/");
			break;
		case TYPE_ARRAY:
			sb.append('[');
			sb.append(getCurrentIndex());
			sb.append(']');
			break;
		case TYPE_OBJECT:
		default:
			sb.append('{');
			String currentName = currentName();
			if (currentName != null) {
				sb.append('"');
				appendQuoted(sb, currentName);
				sb.append('"');
			} else {
				sb.append('?');
			}
			sb.append('}');
			break;
		}
		return sb.toString();
	}

	protected final static char[] HC = "0123456789ABCDEF".toCharArray();

	public final static int ESCAPE_STANDARD = -1;

	protected final static int[] sOutputEscapes128NoSlash;
	static {
		int[] table = new int[128];
		// Control chars need generic escape sequence
		for (int i = 0; i < 32; ++i) {
			// 04-Mar-2011, tatu: Used to use "-(i + 1)", replaced with constant
			table[i] = ESCAPE_STANDARD;
		}
		// Others (and some within that range too) have explicit shorter sequences
		table['"'] = '"';
		table['\\'] = '\\';
		// Escaping of slash is optional, so let's not add it
		table[0x08] = 'b';
		table[0x09] = 't';
		table[0x0C] = 'f';
		table[0x0A] = 'n';
		table[0x0D] = 'r';
		sOutputEscapes128NoSlash = table;
	}

	protected final static int[] sOutputEscapes128WithSlash;
	static {
		sOutputEscapes128WithSlash = Arrays.copyOf(sOutputEscapes128NoSlash, sOutputEscapes128NoSlash.length);
		sOutputEscapes128WithSlash['/'] = '/';
	}

	public static void appendQuoted(StringBuilder sb, String content) {
		final int[] escCodes = sOutputEscapes128WithSlash;
		final int escLen = escCodes.length;
		for (int i = 0, len = content.length(); i < len; ++i) {
			char c = content.charAt(i);
			if (c >= escLen || escCodes[c] == 0) {
				sb.append(c);
				continue;
			}
			sb.append('\\');
			int escCode = escCodes[c];
			if (escCode < 0) { // generic quoting (hex value)
				// The only negative value sOutputEscapes128 returns
				// is CharacterEscapes.ESCAPE_STANDARD, which mean
				// appendQuotes should encode using the Unicode encoding;
				// not sure if this is the right way to encode for
				// CharacterEscapes.ESCAPE_CUSTOM or other (future)
				// CharacterEscapes.ESCAPE_XXX values.

				// We know that it has to fit in just 2 hex chars
				sb.append('u');
				sb.append('0');
				sb.append('0');
				int value = c; // widening
				sb.append(HC[value >> 4]);
				sb.append(HC[value & 0xF]);
			} else { // "named", i.e. prepend with slash
				sb.append((char) escCode);
			}
		}
	}

}
