package com.example.network

import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import e
import java.time.LocalDate
import java.time.format.DateTimeParseException

class PromiscuousLocalDateAdapter : CustomTypeAdapter<LocalDate> {
    override fun encode(value: LocalDate): CustomTypeValue<*> =
        CustomTypeValue.fromRawValue(value.toString())

    override fun decode(value: CustomTypeValue<*>): LocalDate = try {
        LocalDate.parse((value.value as String).substring(0, 10))
    } catch (e: DateTimeParseException) {
        e(e)
        throw e
    }
}
