package com.hedvig.app.util.apollo.adapter

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import java.time.LocalDate

/**
 * Parses incoming date strings in a way that loses the time information, even if it is included originally.
 *
 * For example, the string "2011-01-02T05:19" will result in `LocalDate.of(2011, Month.JANUARY, 2)`
 */
class PromiscuousLocalDateAdapter : Adapter<LocalDate> {
    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): LocalDate {
        val date = reader.nextString()!!
        val dateWithoutTimeInformation = date.take(10)
        return LocalDate.parse(dateWithoutTimeInformation)
    }

    override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: LocalDate) {
        writer.value(value.toString())
    }
}
