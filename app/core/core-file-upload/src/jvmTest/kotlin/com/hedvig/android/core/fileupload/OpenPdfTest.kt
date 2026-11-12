package com.hedvig.android.core.fileupload

import com.hedvig.android.logger.TestLogcatLoggingRule
import java.awt.GraphicsEnvironment
import java.io.File
import java.io.IOException
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.junit.Rule

class OpenPdfTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

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
  fun `the default failure report only logs when there is no screen`() {
    assertTrue(GraphicsEnvironment.isHeadless())

    openPdf(file = File(directory, "missing.pdf"))
  }
}
