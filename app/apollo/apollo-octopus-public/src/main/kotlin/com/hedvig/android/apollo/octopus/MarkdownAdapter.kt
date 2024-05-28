
package com.hedvig.android.apollo.octopus

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.hedvig.android.core.markdown.MarkdownString

object MarkdownStringAdapter : Adapter<MarkdownString> {
  override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): MarkdownString {
    return MarkdownString(reader.nextString()!!)
  }

  override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: MarkdownString) {
    writer.value(value.string)
  }
}
