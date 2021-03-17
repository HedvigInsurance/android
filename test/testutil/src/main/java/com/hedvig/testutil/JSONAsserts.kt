package com.hedvig.testutil

import io.mockk.Matcher
import io.mockk.MockKMatcherScope
import org.json.JSONException
import org.json.JSONObject

fun MockKMatcherScope.jsonEq(expected: JSONObject) = match(JSONEqualsMatcher(expected))

data class JSONEqualsMatcher(private val expected: JSONObject) : Matcher<JSONObject> {
    override fun match(arg: JSONObject?) = arg?.let { expected.isEqualTo(it) } == true

    override fun toString() = "eq($expected)"
}

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
