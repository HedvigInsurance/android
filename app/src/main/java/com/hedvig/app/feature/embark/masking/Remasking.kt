package com.hedvig.app.feature.embark.masking

import com.hedvig.app.feature.embark.BIRTH_DATE_REVERSE
import java.time.LocalDate

fun remask(text: String, mask: String?) = when (mask) {
    BIRTH_DATE_REVERSE -> {
        LocalDate.parse(text, ISO_8601_DATE).format(REVERSE_DATE)
    }
    else -> text
}
