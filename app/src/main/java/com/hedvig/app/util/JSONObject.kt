package com.hedvig.app.util

import androidx.core.os.bundleOf
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

fun jsonObjectOf(vararg properties: Pair<String, Any?>) = JSONObject().apply {
    properties.forEach { property ->
        put(property.first, convertValueToJson(property.second))
    }
}

fun Map<*, *>.toJsonObject(): JSONObject = entries.fold(JSONObject()) { acc, entry ->
    val entryKey = entry.key
    if (entryKey !is String) {
        throw IllegalArgumentException("Only `Map<String, Any?>` may be converted to `JSONObject`")
    }
    val entryValue = entry.value
    acc.put(entryKey, convertValueToJson(entryValue))
    acc
}

private fun convertValueToJson(value: Any?) = when (value) {
    is Map<*, *> -> value.toJsonObject()
    is Collection<*> -> value.toJsonArray()
    else -> value
}

/**
 * Converts this `JSONObject` into a `Map`.
 *
 * Note that this extension is not named `toMap` due to a potential
 * conflict with a later version of `JSONObject`.
 */
fun JSONObject.asMap(): Map<String, Any?> = entriesIterable()
    .fold(mutableMapOf()) { acc, (k, v) ->
        acc[k] = convertJsonValue(v)
        acc
    }

fun JSONArray.toList() = values().map { convertJsonValue(it) }.toList()

private fun convertJsonValue(value: Any?): Any? = when (value) {
    JSONObject.NULL, null -> null
    is JSONObject -> value.asMap()
    is JSONArray -> value.toList()
    else -> value
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

fun Collection<*>.toJsonArray() = JSONArray(this)

fun JSONObject.toBundle() = bundleOf(
    *(
        entries()
            .asSequence()
            .map { // Replace `JSONObject`s NULL-markers with true nulls
                if (it.second == JSONObject.NULL) {
                    (it.first to null)
                } else {
                    it
                }
            }
            .toList().toTypedArray()
        )
)

fun JSONObject.entriesIterable() = object : Iterable<Pair<String, Any?>> {
    override fun iterator(): Iterator<Pair<String, Any?>> = entries()
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

fun JSONObject.createAndAddWithLodashNotation(value: String, key: String, currentKey: String): JSONObject {

    fun String.isArray() = contains("[") && contains("]")
    fun String.getArrayField() = substringBefore("[")
    fun String.getField() = if (isArray()) getArrayField() else substringBefore(".")
    fun String.getIndex() = substringAfter("[").substringBefore("]").toInt()
    fun String.tail(current: String) = substringAfter(current).substringAfter(".").substringBefore(".")
    fun String.hasNext() = substringAfter(currentKey).contains(".")

    val field = currentKey.getField()

    return if (key.hasNext()) {
        val innerJson = if (currentKey.isArray()) {
            if (!has(field)) {
                put(field, JSONArray())
            }
            try {
                getJSONArray(field).getJSONObject(currentKey.getIndex())
            } catch (throwable: JSONException) {
                getJSONArray(field).put(currentKey.getIndex(), JSONObject())
                getJSONArray(field).getJSONObject(currentKey.getIndex())
            }
        } else {
            if (!has(field)) {
                put(field, JSONObject())
            }
            getJSONObject(field)
        }
        innerJson.createAndAddWithLodashNotation(value, key, key.tail(currentKey))
    } else {
        put(field, value)
        this
    }
}
