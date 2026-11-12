package com.hedvig.android.compose.photo.capture.state

import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Rule

class CameraUnavailablePhotoCaptureStateTest {
  private val recordingLogger = RecordingLogcatLogger()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule(recordingLogger)

  @Test
  fun `a take photo request does not throw and logs one warning`() {
    CameraUnavailablePhotoCaptureState.launchTakePhotoRequest()

    assertEquals(listOf(LogPriority.WARN), recordingLogger.priorities)
  }
}

private class RecordingLogcatLogger : LogcatLogger {
  val priorities = mutableListOf<LogPriority>()

  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    priorities.add(priority)
  }
}
