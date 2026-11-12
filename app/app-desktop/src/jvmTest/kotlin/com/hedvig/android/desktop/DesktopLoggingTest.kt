package com.hedvig.android.desktop

import assertk.assertThat
import assertk.assertions.containsExactly
import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.ErrorSource
import com.hedvig.android.core.tracking.RumLogger
import com.hedvig.android.core.tracking.logAction
import com.hedvig.android.core.tracking.logError
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import org.junit.After
import org.junit.Test

class DesktopLoggingTest {
  private val recordingLogger = RecordingLogcatLogger()

  @After
  fun tearDown() {
    LogcatLogger.uninstall()
    RumLogger.uninstall()
  }

  @Test
  fun `after the desktop logging is installed, RUM actions and errors are written to logcat instead of throwing`() {
    installDesktopLogging(recordingLogger)

    logAction(ActionType.CUSTOM, "homeQuoteClicked", emptyMap())
    logError("GraphQL errors for Test: [Something failed]", ErrorSource.NETWORK)

    assertThat(recordingLogger.lines).containsExactly(
      LogPriority.DEBUG to "RUM action CUSTOM homeQuoteClicked attributes: {}",
      LogPriority.WARN to "RUM error from NETWORK: GraphQL errors for Test: [Something failed] attributes: {}",
    )
  }
}
