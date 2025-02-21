package com.hedvig.android.logger

import com.hedvig.android.core.tracking.RumLogger
import org.junit.rules.ExternalResource

/**
 * A test rule to install a [TestLogcatLogger] as a [LogcatLogger] for the test class to print to.
 * Just add:
 * ```
 * @get:Rule
 * val testLogcatLogger = TestLogcatLoggingRule()
 * ```
 */
class TestLogcatLoggingRule(
  private val testLogcatLogger: LogcatLogger = TestLogcatLogger(),
  private val testRumLogger: RumLogger = TestRumLogger(),
) : ExternalResource(), LogcatLogger by testLogcatLogger, RumLogger by testRumLogger {
  override fun before() {
    LogcatLogger.install(testLogcatLogger)
    RumLogger.install(testRumLogger)
  }

  override fun after() {
    LogcatLogger.uninstall()
    RumLogger.uninstall()
  }
}
