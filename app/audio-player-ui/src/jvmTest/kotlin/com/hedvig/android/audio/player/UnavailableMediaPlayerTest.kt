package com.hedvig.android.audio.player

import com.hedvig.android.audio.player.data.ProgressPercentage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.test.runTest
import org.junit.Rule

class UnavailableMediaPlayerTest {
  private val recordingLogger = RecordingLogcatLogger()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule(recordingLogger)

  @Test
  fun `creating a player for any url does not throw`() {
    CommonMediaPlayer("https://example.com/recording.m4a")
    CommonMediaPlayer("")
  }

  @Test
  fun `preparing reports an error and never reports ready`() {
    val player = CommonMediaPlayer("https://example.com/recording.m4a")
    val errors = mutableListOf<Pair<Int, Int>>()
    var prepared = false
    player.setOnErrorListener { what, extra ->
      errors.add(what to extra)
      true
    }
    player.setOnPreparedListener { prepared = true }

    player.prepareAsync()

    assertEquals(1, errors.size)
    assertFalse(prepared)
    assertEquals(listOf(LogPriority.WARN), recordingLogger.priorities)
  }

  @Test
  fun `the player never plays and every call is safe`() = runTest {
    val player = CommonMediaPlayer("https://example.com/recording.m4a")

    player.start()
    player.seekToPercent(ProgressPercentage(0.5f))
    player.pause()
    player.stop()
    player.release()

    assertFalse(player.isPlaying)
    assertFalse(player.hasReachedTheEnd())
    assertEquals(ProgressPercentage(0f), player.getProgressPercentage())
  }
}

private class RecordingLogcatLogger : LogcatLogger {
  val priorities = mutableListOf<LogPriority>()

  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    priorities.add(priority)
  }
}
