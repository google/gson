Change Log
==========

## Version 2.4.0

_2015-10-04_

 * **Drop `IOException` from `TypeAdapter.toJson()`.** This is a binary-compatible change, but may
   cause compiler errors where `IOExceptions` are being caught but no longer thrown. The correct fix
   for this problem is to remove the unnecessary `catch` clause.
 * New: `Gson.newJsonWriter` method returns configured `JsonWriter` instances.
 * New: `@SerializedName` now works with [AutoValue’s][autovalue] abstract property methods.
 * New: `@SerializedName` permits alternate names when deserializing.
 * New: `JsonWriter#jsonValue` writes raw JSON values.
 * New: APIs to add primitives directly to `JsonArray` instances.
 * New: ISO 8601 date type adapter. Find this in _extras_.
 * Fix: `FieldNamingPolicy` now works properly when running on a device with a Turkish locale.

  [autovalue]: https://github.com/google/auto/tree/master/value

