package com.hedvig.onboarding.createoffer.masking

import java.time.format.DateTimeFormatter

internal val ISO_8601_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd")
internal val REVERSE_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy")
internal val SHORT_DATE = DateTimeFormatter.ofPattern("E, d MMM")
