package com.hedvig.app.util

import org.json.JSONArray
import org.json.JSONObject

fun jsonObjectOf(vararg properties: Pair<String, Any>) = JSONObject().apply {
    properties.forEach { put(it.first, it.second) }
}

fun jsonObjectOfNotNull(vararg properties: Pair<String, Any>?) =
    jsonObjectOf(*(properties.filterNotNull().toTypedArray()))

fun List<Pair<String, Any>>.toJsonObject() = JSONObject().apply {
    forEach { put(it.first, it.second) }
}

fun JSONObject.getWithDotNotation(accessor: String): Any {
    if (accessor.contains('.')) {
        val firstAccessor = accessor.substringBefore('.')
        val nextAccessor = accessor.substringAfter('.')

        return getJSONObject(firstAccessor).getWithDotNotation(nextAccessor)
    }

    return get(accessor)
}

fun Collection<Any>.toJsonArray() = JSONArray(this)
