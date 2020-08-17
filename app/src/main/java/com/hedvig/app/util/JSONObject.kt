package com.hedvig.app.util

import org.json.JSONArray
import org.json.JSONObject

fun jsonObjectOf(vararg properties: Pair<String, Any>) = JSONObject().apply {
    properties.forEach { put(it.first, it.second) }
}

fun Collection<Any>.toJsonArray() = JSONArray(this)
