package com.hedvig.app.util

import org.json.JSONObject

fun jsonObjectOf(vararg properties: Pair<String, Any?>) = JSONObject().apply {
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

operator fun JSONObject.plus(other: JSONObject): JSONObject {
    val clone = JSONObject(toString())
    other.entries().forEach { (key, value) ->
        clone.put(key, value)
    }
    return clone
}
