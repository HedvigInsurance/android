package com.hedvig.app.apollo

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.api.json.buildJsonString
import com.apollographql.apollo3.api.toJson
import com.apollographql.apollo3.api.toJsonString
import com.hedvig.app.util.apollo.adapter.CUSTOM_SCALAR_ADAPTERS

fun Operation.Data.toJsonStringWithData(
    customScalarAdapters: CustomScalarAdapters = CUSTOM_SCALAR_ADAPTERS,
): String {
    return buildJsonString {
        beginObject()
        name("data")
        this@toJsonStringWithData.toJson(jsonWriter = this@buildJsonString, customScalarAdapters)
        endObject()
    }
}

// toJsonString gives a json representation, meaning there's extra '"' surrounding the json. This removes it since the
//  test builders require the json representation without those extra quotation marks.
fun <T> Adapter<T>.toJsonStringForTestBuilder(
    data: T,
    customScalarAdapters: CustomScalarAdapters = CUSTOM_SCALAR_ADAPTERS,
): String {
    val jsonString = toJsonString(data, customScalarAdapters)
    return if ((jsonString.first() == '"') && (jsonString.last() == '"')) {
        jsonString.drop(1).dropLast(1)
    } else {
        jsonString
    }
}
