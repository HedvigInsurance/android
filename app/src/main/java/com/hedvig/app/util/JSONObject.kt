package com.hedvig.app.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

fun jsonObjectOf(vararg properties: Pair<String, Any?>) = JSONObject().apply {
    properties.forEach { put(it.first, it.second) }
}

fun jsonArrayOf(vararg items: Any) = JSONArray().apply {
    items.forEach { put(it) }
}

fun jsonObjectOfNotNull(vararg properties: Pair<String, Any>?) =
    jsonObjectOf(*(properties.filterNotNull().toTypedArray()))

fun List<Pair<String, Any>>.toJsonObject() = JSONObject().apply {
    forEach { put(it.first, it.second) }
}

fun JSONObject.getWithDotNotation(accessor: String): Any? {
    return try {
        if (accessor.contains('.')) {
            val firstAccessor = accessor.substringBefore('.')
            val nextAccessor = accessor.substringAfter('.')

            getJSONObject(firstAccessor).getWithDotNotation(nextAccessor)
        } else {
            get(accessor)
        }
    } catch (e: JSONException) {
        null
    }
}

fun Collection<Any>.toJsonArray() = JSONArray(this)

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

fun JSONArray.toStringArray(): List<String> {
    return this.values().map(Any::toString)
}

fun JSONArray.values() = object : Iterable<Any> {
    override fun iterator() = JSONArrayEntryIterator(this@values)
}

class JSONArrayEntryIterator(
    private val jsonArray: JSONArray
) : Iterator<Any> {

    private var current = 0
    private var size = jsonArray.length()

    override fun hasNext() = current < size

    override fun next(): Any {
        if (current >= size) {
            throw NoSuchElementException("")
        }
        val json = jsonArray[current]
        current += 1
        return json
    }
}
