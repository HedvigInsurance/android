package com.hedvig.android.logger

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IosLogcatLoggerTest {
  @AfterTest
  fun uninstall() {
    LogcatLogger.uninstall()
  }

  @Test
  fun `a WARN minimum drops lower priorities without building their message and keeps WARN and above`() {
    val written = mutableListOf<String>()
    val built = mutableListOf<LogPriority>()
    val logger = IosLogcatLogger(LogPriority.WARN) { priority, tag, message -> written += "$priority $tag $message" }

    for (priority in LogPriority.entries) {
      logger.log(priority, null, "Tag") {
        built += priority
        priority.name
      }
    }

    assertEquals(listOf(LogPriority.WARN, LogPriority.ERROR, LogPriority.ASSERT), built)
    assertEquals(listOf("WARN Tag WARN", "ERROR Tag ERROR", "ASSERT Tag ASSERT"), written)
  }

  @Test
  fun `a VERBOSE minimum keeps every priority`() {
    val written = mutableListOf<LogPriority>()
    val logger = IosLogcatLogger(LogPriority.VERBOSE) { priority, _, _ -> written += priority }

    for (priority in LogPriority.entries) {
      logger.log(priority, null, null) { priority.name }
    }

    assertEquals(LogPriority.entries.toList(), written.toList())
  }

  @Test
  fun `a throwable reaches the sink as its class name only`() {
    val sink = RecordingSink()
    IosLogcatLogger.install(sink, LogPriority.VERBOSE)

    logcat(LogPriority.ERROR, IllegalStateException("member-sentinel-7a1d"), tag = "Tag") { "Failed" }

    assertEquals(listOf("ERROR Tag Failed (IllegalStateException)"), sink.lines)
  }

  @Test
  fun `the sink replaces a logger installed before it without a warning line`() {
    IosLogcatLogger.install()
    val sink = RecordingSink()

    IosLogcatLogger.install(sink, LogPriority.INFO)
    logcat(LogPriority.INFO) { "After" }
    logcat(LogPriority.DEBUG) { "Dropped" }

    assertEquals(listOf("INFO null After"), sink.lines)
  }

  private class RecordingSink : IosLogSink {
    val lines = mutableListOf<String>()

    override fun log(priority: LogPriority, tag: String?, message: String) {
      lines += "$priority $tag $message"
    }
  }
}
