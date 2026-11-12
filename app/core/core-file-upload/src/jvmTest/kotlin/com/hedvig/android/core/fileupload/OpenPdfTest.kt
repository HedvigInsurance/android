package com.hedvig.android.core.fileupload

import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.logger.TestLogcatLoggingRule
import java.io.File
import java.io.IOException
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.junit.Rule

class OpenPdfTest {
  private val recordingLogger = RecordingLogcatLogger()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule(recordingLogger)

  private val directory = createTempDirectory("open-pdf-test").toFile()
  private val pdf = File(directory, "certificate.pdf").apply { writeBytes(byteArrayOf(1, 2, 3)) }

  private class Recorder {
    val opened = mutableListOf<File>()
    val failures = mutableListOf<Pair<File, Throwable?>>()
  }

  private fun Recorder.openPdf(
    file: File = pdf,
    isDesktopSupported: Boolean = true,
    isOpenSupported: Boolean = true,
    open: (File) -> Unit = { opened.add(it) },
  ) {
    openPdf(
      file = file,
      isDesktopSupported = { isDesktopSupported },
      isOpenSupported = { isOpenSupported },
      open = open,
      reportFailure = { file, cause -> failures.add(file to cause) },
    )
  }

  @Test
  fun `an existing file is opened and no failure is reported`() {
    val recorder = Recorder()

    recorder.openPdf()

    assertEquals(listOf(pdf), recorder.opened)
    assertTrue(recorder.failures.isEmpty())
  }

  @Test
  fun `a missing file reports a failure without opening anything`() {
    val recorder = Recorder()
    val missing = File(directory, "missing.pdf")

    recorder.openPdf(file = missing)

    assertTrue(recorder.opened.isEmpty())
    assertEquals(listOf<Pair<File, Throwable?>>(missing to null), recorder.failures)
  }

  @Test
  fun `an unsupported desktop reports a failure without opening anything`() {
    val recorder = Recorder()

    recorder.openPdf(isDesktopSupported = false)

    assertTrue(recorder.opened.isEmpty())
    assertEquals(listOf<Pair<File, Throwable?>>(pdf to null), recorder.failures)
  }

  @Test
  fun `an unsupported open action reports a failure without opening anything`() {
    val recorder = Recorder()

    recorder.openPdf(isOpenSupported = false)

    assertTrue(recorder.opened.isEmpty())
    assertEquals(listOf<Pair<File, Throwable?>>(pdf to null), recorder.failures)
  }

  @Test
  fun `an IOException from the viewer launch is reported, not thrown`() {
    assertOpenFailureIsReported(IOException("no application is associated with pdf"))
  }

  @Test
  fun `an UnsupportedOperationException from the viewer launch is reported, not thrown`() {
    assertOpenFailureIsReported(UnsupportedOperationException("open is not supported"))
  }

  @Test
  fun `a SecurityException from the viewer launch is reported, not thrown`() {
    assertOpenFailureIsReported(SecurityException("denied"))
  }

  private fun assertOpenFailureIsReported(exception: Exception) {
    val recorder = Recorder()

    recorder.openPdf(open = { throw exception })

    assertEquals(1, recorder.failures.size)
    assertEquals(pdf, recorder.failures.single().first)
    assertSame(exception, recorder.failures.single().second)
  }

  @Test
  fun `an IllegalArgumentException from the viewer launch is reported, not thrown`() {
    assertOpenFailureIsReported(IllegalArgumentException("not a file"))
  }

  @Test
  fun `the failure report logs a warning and shows no dialog when there is no screen`() {
    val dialogMessages = mutableListOf<String>()

    reportOpenPdfFailure(
      file = pdf,
      cause = null,
      isHeadless = { true },
      showDialog = { dialogMessages.add(it) },
    )

    assertTrue(dialogMessages.isEmpty())
    assertEquals(listOf(LogPriority.WARN), recordingLogger.priorities)
  }

  @Test
  fun `the failure report shows one dialog with the file's path when there is a screen`() {
    val dialogMessages = mutableListOf<String>()

    reportOpenPdfFailure(
      file = pdf,
      cause = null,
      isHeadless = { false },
      showDialog = { dialogMessages.add(it) },
    )

    assertEquals(1, dialogMessages.size)
    assertTrue(dialogMessages.single().contains(pdf.absolutePath))
    assertEquals(listOf(LogPriority.WARN), recordingLogger.priorities)
  }
}

private class RecordingLogcatLogger : LogcatLogger {
  val priorities = mutableListOf<LogPriority>()

  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    priorities.add(priority)
  }
}
