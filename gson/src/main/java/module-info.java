/**
 * Defines the Gson serialization/deserialization API.
 * @since 2.8.6
 */
module com.google.gson {
	exports com.google.gson;
	exports com.google.gson.annotations;
	exports com.google.gson.reflect;
	exports com.google.gson.stream;

	requires transitive java.sql;
}
