package com.hedvig.app.util

import java.time.Period
import java.time.temporal.TemporalAmount

inline val Int.months: TemporalAmount
    get() = Period.ofMonths(this)
