package com.hedvig.app.apollo

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.api.json.buildJsonString
import com.apollographql.apollo3.api.toJson
import com.apollographql.apollo3.api.toJsonString

fun Operation.Data.toJsonStringWithData(
    customScalarAdapters: CustomScalarAdapters = CustomScalarAdapters.Empty,
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
fun <T> Adapter<T>.toJsonStringForTestBuilder(data: T): String {
    val jsonString = toJsonString(data)
    return if ((jsonString.first() == '"') && (jsonString.last() == '"')) {
        jsonString.drop(1).dropLast(1)
    } else {
        jsonString
    }
}
