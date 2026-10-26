package com.google.gson;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;

public class JsonPointer implements Serializable {
	/**
	 * Escape character {@value #ESC} per
	 * <a href="https://datatracker.ietf.org/doc/html/rfc6901">RFC6901</a>.
	 * 
	 * <pre>
	 * escaped         = "~" ( "0" / "1" )
	 *  ; representing '~' and '/', respectively
	 * </pre>
	 *
	 * @since 2.17
	 */
	public static final char ESC = '~';

	/**
	 * Escaped slash string {@value #ESC_TILDE} per
	 * <a href="https://datatracker.ietf.org/doc/html/rfc6901">RFC6901</a>.
	 * 
	 * <pre>
	 * escaped         = "~" ( "0" / "1" )
	 *  ; representing '~' and '/', respectively
	 * </pre>
	 *
	 * @since 2.17
	 */
	public static final String ESC_SLASH = "~1";

	/**
	 * Escaped tilde string {@value #ESC_TILDE} per
	 * <a href="https://datatracker.ietf.org/doc/html/rfc6901">RFC6901</a>.
	 * 
	 * <pre>
	 * escaped         = "~" ( "0" / "1" )
	 *  ; representing '~' and '/', respectively
	 * </pre>
	 *
	 * @since 2.17
	 */
	public static final String ESC_TILDE = "~0";

	private static final long serialVersionUID = 1L;

	/**
	 * Character used to separate segments.
	 * 
	 * <pre>
	 * json-pointer    = *( "/" reference-token )
	 * </pre>
	 *
	 * @since 2.9
	 */
	public final static char SEPARATOR = '/';

	/**
	 * Marker instance used to represent segment that matches current node or
	 * position (that is, returns true for {@link #matches()}).
	 */
	protected final static JsonPointer EMPTY = new JsonPointer();

	/**
	 * Reference to rest of the pointer beyond currently matching segment (if any);
	 * null if this pointer refers to the matching segment.
	 */
	protected final JsonPointer _nextSegment;

	/**
	 * Reference from currently matching segment (if any) to node before leaf.
	 * Lazily constructed if/as needed.
	 * <p>
	 * NOTE: we'll use `volatile` here assuming that this is unlikely to become a
	 * performance bottleneck. If it becomes one we can probably just drop it and
	 * things still should work (despite warnings as per JMM regarding visibility
	 * (and lack thereof) of unguarded changes).
	 *
	 * @since 2.5
	 */
	protected volatile JsonPointer _head;

	/**
	 * We will retain representation of the pointer, as a String, so that
	 * {@link #toString} should be as efficient as possible.
	 * <p>
	 * NOTE: starting with 2.14, there is now accompanying {@link #_asStringOffset}
	 * that MUST be considered with this String; this {@code String} may contain
	 * preceding path, as it is now full path of parent pointer, except for the
	 * outermost pointer instance.
	 */
	protected final String _asString;

	/**
	 * @since 2.14
	 */
	protected final int _asStringOffset;

	protected final String _matchingPropertyName;

	protected final int _matchingElementIndex;

	/**
	 * Lazily-calculated hash code: need to retain hash code now that we can no
	 * longer rely on {@link #_asString} being the exact full representation (it is
	 * often "more", including parent path).
	 *
	 * @since 2.14
	 */
	protected int _hashCode;

	/*
	 * /********************************************************** /* Construction
	 * /**********************************************************
	 */

	/**
	 * Constructor used for creating "empty" instance, used to represent state that
	 * matches current node.
	 */
	protected JsonPointer() {
		_nextSegment = null;
		// [core#788]: must be `null` to distinguish from Property with "" as key
		_matchingPropertyName = null;
		_matchingElementIndex = -1;
		_asString = "";
		_asStringOffset = 0;
	}

	// Constructor used for creating non-empty Segments
	protected JsonPointer(String fullString, int fullStringOffset, String segment, JsonPointer next) {
		_asString = fullString;
		_asStringOffset = fullStringOffset;
		_nextSegment = next;
		// Ok; may always be a property
		_matchingPropertyName = segment;
		// but could be an index, if parsable
		_matchingElementIndex = _parseIndex(segment);
	}

	protected JsonPointer(String fullString, int fullStringOffset, String segment, int matchIndex, JsonPointer next) {
		_asString = fullString;
		_asStringOffset = fullStringOffset;
		_nextSegment = next;
		_matchingPropertyName = segment;
		_matchingElementIndex = matchIndex;
	}

	/**
	 * Copy-constructor used for creating transformed instances with re-linking
	 * textual contents to new "next" pointer instance.
	 *
	 * @param src  Original pointer to copy "full String" from
	 * @param next New "next" pointed to link to
	 *
	 * @since 2.19
	 */
	protected JsonPointer(JsonPointer src, JsonPointer next) {
		_asString = src._asString;
		_asStringOffset = src._asStringOffset;
		_nextSegment = next;
		_matchingPropertyName = src._matchingPropertyName;
		_matchingElementIndex = src._matchingElementIndex;
	}

	/**
	 * Copy-constructor used for creating transformed instances without "next"
	 * linkage
	 *
	 * @param src                 Original pointer to copy "matchingXxx" fields from
	 * @param newFullString       Full String to use
	 * @param newFullStringOffset Offset for new full String to use
	 *
	 * @since 2.19
	 */
	protected JsonPointer(JsonPointer src, String newFullString, int newFullStringOffset) {
		_asString = newFullString;
		_asStringOffset = newFullStringOffset;
		_nextSegment = null;
		_matchingPropertyName = src._matchingPropertyName;
		_matchingElementIndex = src._matchingElementIndex;
	}

	/*
	 * /********************************************************** /* Factory
	 * methods /**********************************************************
	 */

	/**
	 * Factory method that parses given input and construct matching pointer
	 * instance, if it represents a valid JSON Pointer: if not, a
	 * {@link IllegalArgumentException} is thrown.
	 *
	 * @param expr Pointer expression to compile
	 *
	 * @return Compiled {@link JsonPointer} path expression
	 *
	 * @throws IllegalArgumentException Thrown if the input does not present a valid
	 *                                  JSON Pointer expression: currently the only
	 *                                  such expression is one that does NOT start
	 *                                  with a slash ('/').
	 */
	public static JsonPointer compile(String expr) throws IllegalArgumentException {
		// First quick checks for well-known 'empty' pointer
		if ((expr == null) || expr.length() == 0) {
			return EMPTY;
		}
		// And then quick validity check:
		if (expr.charAt(0) != SEPARATOR) {
			throw new IllegalArgumentException(
					"Invalid input: JSON Pointer expression must start with '/': " + "\"" + expr + "\"");
		}
		return _parseTail(expr);
	}

	/**
	 * Alias for {@link #compile}; added to make instances automatically
	 * deserializable by Jackson databind.
	 *
	 * @param expr Pointer expression to compile
	 *
	 * @return Compiled {@link JsonPointer} path expression
	 */
	public static JsonPointer valueOf(String expr) {
		return compile(expr);
	}

	/**
	 * Accessor for an "empty" expression, that is, one you can get by calling
	 * {@link #compile} with "" (empty String).
	 * <p>
	 * NOTE: this is different from expression for {@code "/"} which would instead
	 * match Object node property with empty String ("") as name.
	 *
	 * @return "Empty" pointer expression instance that matches given root value
	 *
	 * @since 2.10
	 */
	public static JsonPointer empty() {
		return EMPTY;
	}

	private static void _appendEscaped(StringBuilder sb, String segment) {
		for (int i = 0, end = segment.length(); i < end; ++i) {
			char c = segment.charAt(i);
			if (c == SEPARATOR) {
				sb.append(ESC_SLASH);
				continue;
			}
			if (c == ESC) {
				sb.append(ESC_TILDE);
				continue;
			}
			sb.append(c);
		}
	}

	/*
	 * /********************************************************** /* Public API
	 * /**********************************************************
	 */

	/**
	 * Functionally same as: <code>
	 *  toString().length()
	 *</code> but more efficient as it avoids likely String allocation.
	 *
	 * @return Length of String representation of this pointer instance
	 *
	 * @since 2.14
	 */
	public int length() {
		return _asString.length() - _asStringOffset;
	}

	public boolean matches() {
		return _nextSegment == null;
	}

	public String getMatchingProperty() {
		return _matchingPropertyName;
	}

	public int getMatchingIndex() {
		return _matchingElementIndex;
	}

	/**
	 * @return True if the root selector matches property name (that is, could match
	 *         field value of JSON Object node)
	 */
	public boolean mayMatchProperty() {
		return _matchingPropertyName != null;
	}

	/**
	 * @return True if the root selector matches element index (that is, could match
	 *         an element of JSON Array node)
	 */
	public boolean mayMatchElement() {
		return _matchingElementIndex >= 0;
	}

	/**
	 * @return the leaf of current JSON Pointer expression: leaf is the last
	 *         non-null segment of current JSON Pointer.
	 *
	 * @since 2.5
	 */
	public JsonPointer last() {
		JsonPointer current = this;
		if (current == EMPTY) {
			return null;
		}
		JsonPointer next;
		while ((next = current._nextSegment) != JsonPointer.EMPTY) {
			current = next;
		}
		return current;
	}

	/**
	 * Mutant factory method that will return
	 * <ul>
	 * <li>`tail` if `this` instance is "empty" pointer, OR</li>
	 * <li>`this` instance if `tail` is "empty" pointer, OR</li>
	 * <li>Newly constructed {@link JsonPointer} instance that starts with all
	 * segments of `this`, followed by all segments of `tail`.</li>
	 * </ul>
	 *
	 * @param tail {@link JsonPointer} instance to append to this one, to create a
	 *             new pointer instance
	 *
	 * @return Either `this` instance, `tail`, or a newly created combination, as
	 *         per description above.
	 */
	public JsonPointer append(JsonPointer tail) {
		if (this == EMPTY) {
			return tail;
		}
		if (tail == EMPTY) {
			return this;
		}
		// 21-Mar-2017, tatu: Not superbly efficient; could probably improve by not
		// concatenating,
		// re-decoding -- by stitching together segments -- but for now should be fine.

		String currentJsonPointer = toString();

		// 14-Dec-2023, tatu: Pre-2.17 had special handling which makes no sense:
		/*
		 * if (currentJsonPointer.endsWith("/")) { //removes final slash
		 * currentJsonPointer = currentJsonPointer.substring(0,
		 * currentJsonPointer.length()-1); }
		 */
		return compile(currentJsonPointer + tail.toString());
	}

	/**
	 * ATTENTION! {@link JsonPointer} is head-centric, tail appending is much
	 * costlier than head appending. It is recommended that this method is used
	 * sparingly due to possible sub-par performance.
	 *
	 * Mutant factory method that will return:
	 * <ul>
	 * <li>`this` instance if `property` is null, OR</li>
	 * <li>Newly constructed {@link JsonPointer} instance that starts with all
	 * segments of `this`, followed by new segment of 'property' name.</li>
	 * </ul>
	 * 'property' is name to match: value is escaped as necessary (for any contained
	 * slashes or tildes).
	 * <p>
	 * NOTE! Before Jackson 2.17, no escaping was performed, and leading slash was
	 * dropped if passed. This was incorrect implementation. Empty {@code property}
	 * was also ignored (similar to {@code null}).
	 *
	 * @param property new segment property name
	 *
	 * @return Either `this` instance, or a newly created combination, as per
	 *         description above.
	 */
	public JsonPointer appendProperty(String property) {
		if (property == null) {
			return this;
		}
		// 14-Dec-2023, tatu: [core#1145] Must escape `property`; accept empty String
		// as valid segment to match as well
		StringBuilder sb = toStringBuilder(property.length() + 2).append(SEPARATOR);
		_appendEscaped(sb, property);
		return compile(sb.toString());
	}

	/**
	 * ATTENTION! {@link JsonPointer} is head-centric, tail appending is much
	 * costlier than head appending. It is recommended that this method is used
	 * sparingly due to possible sub-par performance.
	 *
	 * Mutant factory method that will return newly constructed {@link JsonPointer}
	 * instance that starts with all segments of `this`, followed by new segment of
	 * element 'index'. Element 'index' should be non-negative.
	 *
	 * @param index new segment element index
	 *
	 * @return Newly created combination, as per description above.
	 * @throws IllegalArgumentException if element index is negative
	 */
	public JsonPointer appendIndex(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("Negative index cannot be appended");
		}
		// 14-Dec-2024, tatu: Used to have odd logic for removing "trailing" slash;
		// removed from 2.17
		return compile(toStringBuilder(8).append(SEPARATOR).append(index).toString());
	}

	/**
	 * Method that may be called to see if the pointer head (first segment) would
	 * match property (of a JSON Object) with given name.
	 *
	 * @param name Name of Object property to match
	 *
	 * @return {@code True} if the pointer head matches specified property name
	 *
	 * @since 2.5
	 */
	public boolean matchesProperty(String name) {
		return (_nextSegment != null) && _matchingPropertyName.equals(name);
	}

	/**
	 * Method that may be called to check whether the pointer head (first segment)
	 * matches specified Object property (by name) and if so, return
	 * {@link JsonPointer} that represents rest of the path after match. If there is
	 * no match, {@code null} is returned.
	 *
	 * @param name Name of Object property to match
	 *
	 * @return Remaining path after matching specified property, if there is match;
	 *         {@code null} otherwise
	 */
	public JsonPointer matchProperty(String name) {
		if ((_nextSegment != null) && _matchingPropertyName.equals(name)) {
			return _nextSegment;
		}
		return null;
	}

	/**
	 * Method that may be called to see if the pointer would match Array element (of
	 * a JSON Array) with given index.
	 *
	 * @param index Index of Array element to match
	 *
	 * @return {@code True} if the pointer head matches specified Array index
	 *
	 * @since 2.5
	 */
	public boolean matchesElement(int index) {
		return (index == _matchingElementIndex) && (index >= 0);
	}

	/**
	 * Method that may be called to check whether the pointer head (first segment)
	 * matches specified Array index and if so, return {@link JsonPointer} that
	 * represents rest of the path after match. If there is no match, {@code null}
	 * is returned.
	 *
	 * @param index Index of Array element to match
	 *
	 * @return Remaining path after matching specified index, if there is match;
	 *         {@code null} otherwise
	 *
	 * @since 2.6
	 */
	public JsonPointer matchElement(int index) {
		if ((index != _matchingElementIndex) || (index < 0)) {
			return null;
		}
		return _nextSegment;
	}

	/**
	 * Accessor for getting a "sub-pointer" (or sub-path), instance where current
	 * segment has been removed and pointer includes rest of the segments. For
	 * example, for JSON Pointer "/root/branch/leaf", this method would return
	 * pointer "/branch/leaf". For matching state (last segment), will return
	 * {@code null}.
	 * <p>
	 * Note that this is a very cheap method to call as it simply returns "next"
	 * segment (which has been constructed when pointer instance was constructed).
	 *
	 * @return Tail of this pointer, if it has any; {@code null} if this pointer
	 *         only has the current segment
	 */
	public JsonPointer tail() {
		return _nextSegment;
	}

	/**
	 * Accessor for getting a pointer instance that is identical to this instance
	 * except that the last segment has been dropped. For example, for JSON Pointer
	 * "/root/branch/leaf", this method would return pointer "/root/branch"
	 * (compared to {@link #tail()} that would return "/branch/leaf").
	 * <p>
	 * Note that whereas {@link #tail} is a very cheap operation to call (as "tail"
	 * already exists for single-linked forward direction), this method has to fully
	 * construct a new instance by traversing the chain of segments.
	 *
	 * @return Pointer expression that contains same segments as this one, except
	 *         for the last segment.
	 *
	 * @since 2.5
	 */
	public JsonPointer head() {
		JsonPointer h = _head;
		if (h == null) {
			if (this != EMPTY) {
				h = _constructHead();
			}
			_head = h;
		}
		return h;
	}

	/*
	 * /********************************************************** /* Standard
	 * method overrides (since 2.14)
	 * /**********************************************************
	 */

	@Override
	public String toString() {
		if (_asStringOffset <= 0) {
			return _asString;
		}
		return _asString.substring(_asStringOffset);
	}

	/**
	 * Functionally equivalent to:
	 * 
	 * <pre>
	 * new StringBuilder(toString());
	 * </pre>
	 * 
	 * but possibly more efficient
	 *
	 * @param slack Number of characters to reserve in StringBuilder beyond minimum
	 *              copied
	 * @return a new StringBuilder
	 *
	 * @since 2.17
	 */
	protected StringBuilder toStringBuilder(int slack) {
		if (_asStringOffset <= 0) {
			return new StringBuilder(_asString);
		}
		final int len = _asString.length();
		StringBuilder sb = new StringBuilder(len - _asStringOffset + slack);
		sb.append(_asString, _asStringOffset, len);
		return sb;
	}

	@Override
	public int hashCode() {
		int h = _hashCode;
		if (h == 0) {
			// Alas, this is bit wasteful, creating temporary String, but
			// without JDK exposing hash code calculation for a sub-string
			// can't do much
			h = toString().hashCode();
			if (h == 0) {
				h = -1;
			}
			_hashCode = h;
		}
		return h;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o == null)
			return false;
		if (!(o instanceof JsonPointer))
			return false;
		JsonPointer other = (JsonPointer) o;
		// 07-Oct-2022, tatu: Ugh.... this gets way more complicated as we MUST
		// compare logical representation so cannot simply compare offset
		// and String
		return _compare(_asString, _asStringOffset, other._asString, other._asStringOffset);
	}

	private final boolean _compare(String str1, int offset1, String str2, int offset2) {
		final int end1 = str1.length();

		// Different lengths? Not equal
		if ((end1 - offset1) != (str2.length() - offset2)) {
			return false;
		}

		for (; offset1 < end1;) {
			if (str1.charAt(offset1++) != str2.charAt(offset2++)) {
				return false;
			}
		}

		return true;
	}

	/*
	 * /********************************************************** /* Internal
	 * methods /**********************************************************
	 */

	private final static int _parseIndex(String str) {
		final int len = str.length();
		// [core#133]: beware of super long indexes; assume we never
		// have arrays over 2 billion entries so ints are fine.
		if (len == 0 || len > 10) {
			return -1;
		}
		// [core#176]: no leading zeroes allowed
		char c = str.charAt(0);
		if (c <= '0') {
			return (len == 1 && c == '0') ? 0 : -1;
		}
		if (c > '9') {
			return -1;
		}
		for (int i = 1; i < len; ++i) {
			c = str.charAt(i);
			if (c > '9' || c < '0') {
				return -1;
			}
		}
		if (len == 10) {
			long l = Long.parseLong(str);
			if (l > Integer.MAX_VALUE) {
				return -1;
			}
			return (int) l;
		}
		return Integer.parseInt(str);
	}

	protected static JsonPointer _parseTail(final String fullPath) {
		PointerParent parent = null;

		// first char is the contextual slash, skip
		int i = 1;
		final int end = fullPath.length();
		int startOffset = 0;

		while (i < end) {
			char c = fullPath.charAt(i);
			if (c == SEPARATOR) { // common case, got a segment
				parent = new PointerParent(parent, startOffset, fullPath.substring(startOffset + 1, i));
				startOffset = i;
				++i;
				continue;
			}
			++i;
			// quoting is different; offline this case
			if (c == ESC && i < end) { // possibly, quote
				// 04-Oct-2022, tatu: Let's decode escaped segment
				// instead of recursive call
				StringBuilder sb = new StringBuilder(32);
				i = _extractEscapedSegment(fullPath, startOffset + 1, i, sb);
				final String segment = sb.toString();
				if (i < 0) { // end!
					return _buildPath(fullPath, startOffset, segment, parent);
				}
				parent = new PointerParent(parent, startOffset, segment);
				startOffset = i;
				++i;
				continue;
			}
			// otherwise, loop on
		}
		// end of the road, no escapes
		return _buildPath(fullPath, startOffset, fullPath.substring(startOffset + 1), parent);
	}

	private static JsonPointer _buildPath(final String fullPath, int fullPathOffset, String segment,
			PointerParent parent) {
		JsonPointer curr = new JsonPointer(fullPath, fullPathOffset, segment, EMPTY);
		for (; parent != null; parent = parent.parent) {
			curr = new JsonPointer(fullPath, parent.fullPathOffset, parent.segment, curr);
		}
		return curr;
	}

	/**
	 * Method called to extract the next segment of the path, in case where we seem
	 * to have encountered a (tilde-)escaped character within segment.
	 *
	 * @param input           Full input for the tail being parsed
	 * @param firstCharOffset Offset of the first character of segment (one after
	 *                        slash)
	 * @param i               Offset to character after tilde
	 * @param sb              StringBuilder into which unquoted segment is added
	 *
	 * @return Offset at which slash was encountered, if any, or -1 if expression
	 *         ended without seeing unescaped slash
	 */
	protected static int _extractEscapedSegment(String input, int firstCharOffset, int i, StringBuilder sb) {
		final int end = input.length();
		final int toCopy = i - 1 - firstCharOffset;
		if (toCopy > 0) {
			sb.append(input, firstCharOffset, i - 1);
		}
		i += _appendEscape(sb, input.charAt(i));
		while (i < end) {
			char c = input.charAt(i);
			if (c == SEPARATOR) { // end is nigh!
				return i;
			}
			++i;
			if (c == ESC && i < end) {
				i += _appendEscape(sb, input.charAt(i));
				continue;
			}
			sb.append(c);
		}
		// end of the road, last segment
		return -1;
	}

	private static int _appendEscape(StringBuilder sb, char c) {
		if (c == '0') {
			sb.append(ESC);
			return 1;
		}
		if (c == '1') {
			sb.append(SEPARATOR);
			return 1;
		}
		// Not a valid escape; just output tilde, do not advance past following char
		sb.append(ESC);
		return 0;
	}

	protected JsonPointer _constructHead() {
		// ok; find out the segment we are to drop
		JsonPointer last = last();
		if (last == this) {
			return EMPTY;
		}

		// Initialize a list to store intermediate JsonPointers in reverse
		ArrayList<JsonPointer> pointers = new ArrayList<>();

		JsonPointer current = this;
		String origFullString = toString();
		// Make sure to share the new full string for path segments
		String fullString = origFullString.substring(0, origFullString.length() - last.length());

		// Also: if there was an offset, must compensate (new String starts at 0)
		final int offsetDiff = -_asStringOffset;

		while (current != last) {
			// NOTE: since we drop from the end we can simply reuse offset (w/ possible
			// modification)
			JsonPointer nextSegment = new JsonPointer(current, fullString, current._asStringOffset + offsetDiff);
			pointers.add(nextSegment);
			current = current._nextSegment;
		}

		// Iteratively build the JsonPointer chain from the list in reverse
		JsonPointer head = EMPTY;
		for (int i = pointers.size() - 1; i >= 0; i--) {
			head = new JsonPointer(pointers.get(i), head);
		}

		return head;
	}

	/*
	 * /********************************************************** /* Helper class
	 * used to replace call stack (2.14+)
	 * /**********************************************************
	 */

	/**
	 * Helper class used to replace call stack when parsing JsonPointer expressions.
	 */
	private static class PointerParent {
		public final PointerParent parent;
		public final int fullPathOffset;
		public final String segment;

		PointerParent(PointerParent pp, int fpo, String sgm) {
			parent = pp;
			fullPathOffset = fpo;
			segment = sgm;
		}
	}

	/**
	 * Helper class used to contain a single segment when constructing JsonPointer
	 * from context.
	 */
	private static class PointerSegment {
		public final PointerSegment next;
		public final String property;
		public final int index;

		// Offset within external buffer, updated when constructing
		public int pathOffset;

		// And we actually need 2-way traversal, it turns out so:
		public PointerSegment prev;

		public PointerSegment(PointerSegment next, String pn, int ix) {
			this.next = next;
			property = pn;
			index = ix;
			// Ok not the cleanest thing but...
			if (next != null) {
				next.prev = this;
			}
		}
	}

	/*
	 * /********************************************************** /* Support for
	 * JDK serialization (2.14+)
	 * /**********************************************************
	 */

	// Since 2.14: needed for efficient JDK serializability
	private Object writeReplace() {
		// 11-Oct-2022, tatu: very important, must serialize just contents!
		return new Serialization(toString());
	}

	/**
	 * This must only exist to allow both final properties and implementation of
	 * Externalizable/Serializable for JsonPointer. Note that here we do not store
	 * offset but simply use (and expect use) full path, from which we need to
	 * decode actual structure.
	 *
	 * @since 2.14
	 */
	static class Serialization implements Externalizable {
		private String _fullPath;

		public Serialization() {
		}

		Serialization(String fullPath) {
			_fullPath = fullPath;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeUTF(_fullPath);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			_fullPath = in.readUTF();
		}

		private Object readResolve() throws ObjectStreamException {
			// NOTE: method handles canonicalization of "empty", as well as other
			// aspects of decoding.
			return compile(_fullPath);
		}
	}

	/**
	 * Factory method that will construct a pointer instance that describes path to
	 * location given {@link JsonStreamContext} points to.
	 *
	 * @param context     Context to build pointer expression for
	 * @param includeRoot Whether to include number offset for virtual "root
	 *                    context" or not.
	 *
	 * @return {@link JsonPointer} path to location of given context
	 *
	 * @since 2.9
	 */
	public static JsonPointer forPath(TokenStreamContext context, boolean includeRoot) {
		// First things first: last segment may be for START_ARRAY/START_OBJECT,
		// in which case it does not yet point to anything, and should be skipped
		if (context == null) {
			return EMPTY;
		}
		// Otherwise if context was just created but is not advanced -- like,
		// opening START_ARRAY/START_OBJECT returned -- drop the empty context.
		if (!context.hasPathSegment()) {
			// Except one special case: do not prune root if we need it
			if (!(includeRoot && context.inRoot() && context.hasCurrentIndex())) {
				context = context.getParent();
			}
		}

		PointerSegment next = null;
		int approxLength = 0;

		for (; context != null; context = context.getParent()) {
			if (context.inObject()) {
				String propName = context.currentName();
				if (propName == null) { // is this legal?
					propName = "";
				}
				approxLength += 2 + propName.length();
				next = new PointerSegment(next, propName, -1);
			} else if (context.inArray() || includeRoot) {
				int ix = context.getCurrentIndex();
				approxLength += 6;
				next = new PointerSegment(next, null, ix);
			}
			// NOTE: this effectively drops ROOT node(s); should have 1 such node,
			// as the last one, but we don't have to care (probably some paths have
			// no root, for example)
		}
		if (next == null) {
			return EMPTY;
		}

		// And here the fun starts! We have the head, need to traverse
		// to compose full path String
		StringBuilder pathBuilder = new StringBuilder(approxLength);
		PointerSegment last = null;

		for (; next != null; next = next.next) {
			// Let's find the last segment as well, for reverse traversal
			last = next;
			next.pathOffset = pathBuilder.length();
			pathBuilder.append(SEPARATOR);
			if (next.property != null) {
				_appendEscaped(pathBuilder, next.property);
			} else {
				pathBuilder.append(next.index);
			}
		}
		final String fullPath = pathBuilder.toString();

		// and then iteratively construct JsonPointer chain in reverse direction
		// (from innermost back to outermost)
		PointerSegment currSegment = last;
		JsonPointer currPtr = EMPTY;

		for (; currSegment != null; currSegment = currSegment.prev) {
			if (currSegment.property != null) {
				currPtr = new JsonPointer(fullPath, currSegment.pathOffset, currSegment.property, currPtr);
			} else {
				int index = currSegment.index;
				currPtr = new JsonPointer(fullPath, currSegment.pathOffset, String.valueOf(index), index, currPtr);
			}
		}

		return currPtr;
	}

}
