package com.hedvig.app.feature.embark.masking

import com.hedvig.app.feature.embark.util.MaskType
import java.time.Clock
import java.time.LocalDate
import java.time.Period

fun MaskType.derivedValues(text: String, key: String, clock: Clock) = when (this) {
    MaskType.BIRTH_DATE_REVERSE -> listOf(
        "$key.Age" to Period.between(LocalDate.parse(text), LocalDate.now(clock)).years.toString()
    )
    else -> emptyList()
}
