package com.hedvig.onboarding.createoffer.masking

import com.hedvig.onboarding.createoffer.BIRTH_DATE_REVERSE
import java.time.LocalDate

fun unmask(text: String, mask: String?): String = when (mask) {
    BIRTH_DATE_REVERSE -> LocalDate.parse(text,
        REVERSE_DATE).format(ISO_8601_DATE)
    else -> text
}
