package com.hedvig.app.util.apollo

import com.apollographql.apollo.response.CustomTypeAdapter
import com.apollographql.apollo.response.CustomTypeValue
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeParseException
import timber.log.Timber

class PromiscuousLocalDateAdapter : CustomTypeAdapter<LocalDate> {
    override fun encode(value: LocalDate): CustomTypeValue<*> =
        CustomTypeValue.fromRawValue(value.toString())

    override fun decode(value: CustomTypeValue<*>): LocalDate? = try {
        LocalDate.parse((value.value as String).substring(0, 10))
    } catch (e: DateTimeParseException) {
        Timber.e(e)
        null
    }
}

