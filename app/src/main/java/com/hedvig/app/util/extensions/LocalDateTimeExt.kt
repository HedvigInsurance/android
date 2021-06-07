package com.hedvig.app.util.extensions

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun Long.epochMillisToLocalDate(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}
