Change Log
==========

## Version 2.4.0

_RELEASE DATE TBA_

 * **Drop `IOException` from `TypeAdapter.toJson()`.** This is a binary-compatible change, but may
   cause compiler errors where `IOExceptions` are being caught but no longer thrown. The correct fix
   for this problem is to remove the unnecessary `catch` clause.
 * New: `Gson.newJsonWriter` method returns configured `JsonWriter` instances.
 * New: `@SerializedName` now works with [AutoValue][autovalue’s] abstract property methods.
 * New: `JsonWriter#jsonValue` writes raw JSON values.
 * New: APIs to add primitives directly to `JsonArray` instances.
 * New: ISO 8601 type adapter. Find this in _extras_.
 * Fix: Fix `FieldNamingPolicy` issues when running in an environment with a Turkish locale.



 [autovalue]: https://github.com/google/auto/tree/master/value
