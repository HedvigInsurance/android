package com.hedvig.app.util.apollo

import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeParseException
import timber.log.Timber

class YearMonthAdapter : CustomTypeAdapter<YearMonth> {
    override fun encode(value: YearMonth): CustomTypeValue<*> =
        CustomTypeValue.fromRawValue(value.toString())

    override fun decode(value: CustomTypeValue<*>): YearMonth? = try {
        YearMonth.parse((value.value as String))
    } catch (e: DateTimeParseException) {
        Timber.e(e)
        null
    }
}
