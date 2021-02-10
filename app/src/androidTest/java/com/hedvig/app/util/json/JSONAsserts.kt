package com.hedvig.app.util.json

import com.hedvig.app.util.entries
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
