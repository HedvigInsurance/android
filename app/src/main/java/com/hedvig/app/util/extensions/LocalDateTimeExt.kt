package com.hedvig.app.util.extensions

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

fun Long.epochMillisToLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}

fun Long.epochMillisToLocalDate(): LocalDate {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault()).toLocalDate()
}


fun LocalDateTime.isToday() = toLocalDate().isEqual(LocalDate.now())

fun LocalDate.isToday() = isEqual(LocalDate.now())
