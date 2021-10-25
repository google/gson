/**
 * Defines the Gson serialization/deserialization API.
 * @since 2.8.6
 */
module com.google.gson {
	exports com.google.gson;
	exports com.google.gson.annotations;
	exports com.google.gson.reflect;
	exports com.google.gson.stream;

	// Optional dependency on java.sql
	requires static java.sql;

	// Optional dependency on jdk.unsupported for JDK's sun.misc.Unsafe
	requires static jdk.unsupported;
}
