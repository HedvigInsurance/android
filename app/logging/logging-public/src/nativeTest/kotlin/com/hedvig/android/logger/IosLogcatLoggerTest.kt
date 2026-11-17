package com.hedvig.android.logger

import kotlin.test.Test
import kotlin.test.assertEquals

class IosLogcatLoggerTest {
  @Test
  fun `a WARN minimum drops lower priorities without building their message and keeps WARN and above`() {
    val written = mutableListOf<String>()
    val built = mutableListOf<LogPriority>()
    val logger = IosLogcatLogger(LogPriority.WARN) { written += it }

    for (priority in LogPriority.entries) {
      logger.log(priority, null, "Tag") {
        built += priority
        priority.name
      }
    }

    assertEquals(listOf(LogPriority.WARN, LogPriority.ERROR, LogPriority.ASSERT), built)
    assertEquals(listOf("W/[Tag] WARN", "E/[Tag] ERROR", "A/[Tag] ASSERT"), written)
  }

  @Test
  fun `a VERBOSE minimum keeps every priority`() {
    val written = mutableListOf<String>()
    val logger = IosLogcatLogger(LogPriority.VERBOSE) { written += it }

    for (priority in LogPriority.entries) {
      logger.log(priority, null, null) { priority.name }
    }

    assertEquals(
      listOf("V/VERBOSE", "D/DEBUG", "I/INFO", "W/WARN", "E/ERROR", "A/ASSERT"),
      written,
    )
  }
}
