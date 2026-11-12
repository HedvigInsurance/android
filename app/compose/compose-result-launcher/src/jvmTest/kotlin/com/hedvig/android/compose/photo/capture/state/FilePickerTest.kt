package com.hedvig.android.compose.photo.capture.state

import com.eygraber.uri.Uri
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.logger.TestLogcatLoggingRule
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Rule

class FilePickerTest {
  private val recordingLogger = RecordingLogcatLogger()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule(recordingLogger)

  private val directory = createTempDirectory("file-picker-test").toFile()

  @Test
  fun `a chosen file becomes a file uri whose path is the file's absolute path`() {
    val names = listOf("receipt.pdf", "my receipt.pdf", "kvitto åäö.jpg", "50% off #1.png")
    for (name in names) {
      val file = File(directory, name).apply { writeBytes(byteArrayOf(1)) }

      val uri = file.toFileUri()

      assertEquals("file", uri.scheme)
      assertEquals(file.absolutePath, uri.path)
      assertEquals(name, uri.path?.substringAfterLast('/'))
    }
  }

  @Test
  fun `a file uri survives the string round trip the upload screens store it in`() {
    val file = File(directory, "kvitto åäö 50% #1.pdf").apply { writeBytes(byteArrayOf(1)) }
    val uri = file.toFileUri()

    val parsed = Uri.parse(uri.toString())

    assertEquals(uri.toString(), parsed.toString())
    assertEquals(file.absolutePath, parsed.path)
    assertTrue(File(parsed.path!!).isFile)
  }

  @Test
  fun `the chosen files are handed over in order`() {
    val first = File(directory, "a.pdf")
    val second = File(directory, "b.png")
    val results = mutableListOf<List<Uri>>()

    launchFilePicker(
      filter = null,
      onResult = { results.add(it) },
      isHeadless = { false },
      showDialog = { listOf(first, second) },
    )

    assertEquals(listOf(listOf(first.toFileUri(), second.toFileUri())), results)
  }

  @Test
  fun `cancelling the dialog hands over nothing`() {
    val results = mutableListOf<List<Uri>>()

    launchFilePicker(filter = null, onResult = {
      results.add(it)
    }, isHeadless = { false }, showDialog = { emptyList() })

    assertTrue(results.isEmpty())
  }

  @Test
  fun `without a screen no dialog is shown, nothing is handed over and one warning is logged`() {
    var dialogShown = false
    val results = mutableListOf<List<Uri>>()

    launchFilePicker(
      filter = null,
      onResult = { results.add(it) },
      isHeadless = { true },
      showDialog = {
        dialogShown = true
        emptyList()
      },
    )

    assertFalse(dialogShown)
    assertTrue(results.isEmpty())
    assertEquals(listOf(LogPriority.WARN), recordingLogger.priorities)
  }

  @Test
  fun `the visual media filter accepts images and videos only, ignoring case`() {
    assertTrue(VisualMediaFilenameFilter.accept(directory, "photo.JPG"))
    assertTrue(VisualMediaFilenameFilter.accept(directory, "clip.mov"))
    assertFalse(VisualMediaFilenameFilter.accept(directory, "receipt.pdf"))
    assertFalse(VisualMediaFilenameFilter.accept(directory, "no-extension"))
  }
}

private class RecordingLogcatLogger : LogcatLogger {
  val priorities = mutableListOf<LogPriority>()

  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    priorities.add(priority)
  }
}
