package com.hedvig.app.util.apollo

import com.apollographql.apollo.response.CustomTypeAdapter
import com.apollographql.apollo.response.CustomTypeValue
import e
import java.time.LocalDate
import java.time.format.DateTimeParseException

class PromiscuousLocalDateAdapter : CustomTypeAdapter<LocalDate> {
    override fun encode(value: LocalDate): CustomTypeValue<*> =
        CustomTypeValue.fromRawValue(value.toString())

    override fun decode(value: CustomTypeValue<*>): LocalDate? = try {
        LocalDate.parse((value.value as String).substring(0, 10))
    } catch (e: DateTimeParseException) {
        e(e)
        null
    }
}

