package com.hedvig.app.util

import org.json.JSONObject

fun jsonObjectOf(vararg properties: Pair<String, Any>) = JSONObject().apply {
    properties.forEach { put(it.first, it.second) }
}
