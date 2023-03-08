package com.hedvig.android.apollo.giraffe.typeadapter

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import org.json.JSONObject

@Suppress("unused") // Used inside the `apollo` block inside build.gradle.kts
internal object JSONStringAdapter : Adapter<JSONObject> {
  override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): JSONObject {
    return JSONObject(reader.nextString()!!)
  }

  override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: JSONObject) {
    writer.value(value.toString())
  }
}
