package com.hedvig.android.desktop

import assertk.assertThat
import assertk.assertions.containsExactly
import com.hedvig.android.logger.LogPriority
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import org.junit.Test

class StdoutLogcatLoggerTest {
  @Test
  fun `against production, DEBUG and VERBOSE lines are dropped and INFO and above are written`() {
    val output = ByteArrayOutputStream()
    val logger = stdoutLogcatLogger(DesktopBackend.Production, PrintStream(output, true))

    logger.log(LogPriority.VERBOSE, null, "Test") { "verbose line" }
    logger.log(LogPriority.DEBUG, null, "Test") { "Response data: member data" }
    logger.log(LogPriority.INFO, null, "Test") { "info line" }
    logger.log(LogPriority.ERROR, null, "Test") { "error line" }

    assertThat(output.writtenMessages()).containsExactly("I/Test: info line", "E/Test: error line")
  }

  @Test
  fun `against staging, every line is written`() {
    val output = ByteArrayOutputStream()
    val logger = stdoutLogcatLogger(DesktopBackend.Staging, PrintStream(output, true))

    logger.log(LogPriority.VERBOSE, null, "Test") { "verbose line" }
    logger.log(LogPriority.DEBUG, null, "Test") { "debug line" }
    logger.log(LogPriority.INFO, null, "Test") { "info line" }

    assertThat(output.writtenMessages()).containsExactly(
      "V/Test: verbose line",
      "D/Test: debug line",
      "I/Test: info line",
    )
  }

  /** The written lines without their leading time stamp. */
  private fun ByteArrayOutputStream.writtenMessages(): List<String> {
    return toString(Charsets.UTF_8).lines().filter { it.isNotEmpty() }.map { it.substringAfter(' ') }
  }
}
