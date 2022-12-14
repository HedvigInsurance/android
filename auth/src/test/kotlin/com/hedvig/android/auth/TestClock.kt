package com.hedvig.android.auth

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

class TestClock : Clock {
  private var now = Clock.System.now()

  @Suppress("unused")
  fun advanceTimeBy(duration: Duration) {
    require(duration > Duration.ZERO)
    now += duration
  }

  override fun now(): Instant {
    return now
  }
}
