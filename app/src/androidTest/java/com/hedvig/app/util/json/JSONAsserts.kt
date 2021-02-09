package com.hedvig.app.util.json

import io.mockk.MockKMatcherScope
import org.json.JSONException
import org.json.JSONObject

inline fun <reified T : JSONObject> MockKMatcherScope.jsonEq(other: JSONObject): T = match { it.isEqualTo(other) }

fun JSONObject.isEqualTo(other: JSONObject): Boolean {
    if (length() != other.length()) {
        return false
    }
    entries().forEach { (key, value) ->
        try {
            if (other.get(key) != value) {
                return false
            }
        } catch (ex: JSONException) {
            return false
        }
    }
    return true
}

fun JSONObject.entries() = JSONObjectEntryIterator(this)

class JSONObjectEntryIterator(
    private val jsonObject: JSONObject,
) : Iterator<Pair<String, Any?>> {
    private val innerIter = jsonObject.keys()

    override fun hasNext() = innerIter.hasNext()
    override fun next(): Pair<String, Any?> {
        val key = innerIter.next()
        return Pair(key, jsonObject.get(key))
    }
}
