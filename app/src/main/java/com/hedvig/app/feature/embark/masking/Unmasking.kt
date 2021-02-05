package com.hedvig.app.feature.embark.masking

import com.hedvig.app.feature.embark.BIRTH_DATE_REVERSE
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun unmask(text: String, mask: String?): String = when (mask) {
    BIRTH_DATE_REVERSE -> LocalDate.parse(text,
        REVERSE_DATE).format(ISO_8601_DATE)
    else -> text
}
