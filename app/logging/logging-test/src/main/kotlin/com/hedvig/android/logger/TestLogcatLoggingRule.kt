package com.hedvig.android.logger

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
) : ExternalResource(), LogcatLogger by testLogcatLogger {
  override fun before() {
    LogcatLogger.install(testLogcatLogger)
  }

  override fun after() {
    LogcatLogger.uninstall()
  }
}
