package com.hedvig.onboarding.createoffer.masking

import com.hedvig.onboarding.createoffer.BIRTH_DATE_REVERSE
import java.time.LocalDate

fun remask(text: String, mask: String?) = when (mask) {
    BIRTH_DATE_REVERSE -> {
        LocalDate.parse(text, ISO_8601_DATE).format(REVERSE_DATE)
    }
    else -> text
}
