package com.hedvig.app.util

import org.json.JSONObject

fun jsonObjectOf(vararg properties: Pair<String, Any>) = JSONObject().apply {
    properties.forEach { put(it.first, it.second) }
}

fun JSONObject.getWithDotNotation(accessor: String): Any {
    if (accessor.contains('.')) {
        val firstAccessor = accessor.substringBefore('.')
        val nextAccessor = accessor.substringAfter('.')

        return getJSONObject(firstAccessor).getWithDotNotation(nextAccessor)
    }

    return get(accessor)
}
