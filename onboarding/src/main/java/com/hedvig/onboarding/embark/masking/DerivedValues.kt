package com.hedvig.onboarding.embark.masking

import com.hedvig.onboarding.embark.BIRTH_DATE_REVERSE
import java.time.Clock
import java.time.LocalDate
import java.time.Period

fun derivedValues(text: String, key: String, mask: String?, clock: Clock) = when (mask) {
    BIRTH_DATE_REVERSE -> listOf(
        "${key}.Age" to Period.between(LocalDate.parse(text), LocalDate.now(clock)).years.toString()
    )
    else -> emptyList()
}
