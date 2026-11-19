package com.hedvig.android.compose.photo.capture.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.eygraber.uri.Uri
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import java.awt.Dialog
import java.awt.FileDialog
import java.awt.Frame
import java.awt.GraphicsEnvironment
import java.awt.KeyboardFocusManager
import java.io.File
import java.io.FilenameFilter

@Composable
actual fun rememberPickMultipleVisualMediaResultLauncher(
  block: (uris: List<com.eygraber.uri.Uri>) -> Unit,
): ResultLauncher {
  val updatedBlock by rememberUpdatedState(block)
  return remember {
    ResultLauncher {
      launchFilePicker(filter = VisualMediaFilenameFilter, onResult = { updatedBlock(it) })
    }
  }
}

@Composable
actual fun rememberGetMultipleContentsResultLauncher(block: (uris: List<Uri>) -> Unit): ResultLauncher {
  val updatedBlock by rememberUpdatedState(block)
  return remember {
    ResultLauncher {
      launchFilePicker(filter = null, onResult = { updatedBlock(it) })
    }
  }
}

/**
 * Lets the member choose files and hands them to [onResult] as `file:` URIs. Choosing nothing hands over nothing.
 *
 * The AWT file dialog is modal: [showDialog] returns once it closes. Called from a click handler on the event
 * dispatch thread, AWT keeps dispatching events in a nested loop meanwhile, so the window keeps painting while
 * input to it is blocked.
 */
internal fun launchFilePicker(
  filter: FilenameFilter?,
  onResult: (List<Uri>) -> Unit,
  isHeadless: () -> Boolean = GraphicsEnvironment::isHeadless,
  showDialog: (FilenameFilter?) -> List<File> = ::showFileDialog,
) {
  if (isHeadless()) {
    logcat(LogPriority.WARN) { "There is no screen to show a file picker on" }
    return
  }
  val files = showDialog(filter)
  if (files.isEmpty()) return
  onResult(files.map { it.toFileUri() })
}

/** A `file:` URI whose [Uri.path] is this file's absolute path. */
internal fun File.toFileUri(): Uri = Uri.parse(absoluteFile.toURI().toString())

internal val VisualMediaFilenameFilter = FilenameFilter { _, name ->
  name.substringAfterLast('.', "").lowercase() in visualMediaExtensions
}

private val visualMediaExtensions = setOf(
  "jpg",
  "jpeg",
  "png",
  "gif",
  "webp",
  "heic",
  "heif",
  "bmp",
  "tif",
  "tiff",
  "mp4",
  "mov",
  "m4v",
  "avi",
  "mkv",
  "webm",
  "3gp",
)

private fun showFileDialog(filter: FilenameFilter?): List<File> {
  val dialog = when (val owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow) {
    is Frame -> FileDialog(owner)
    is Dialog -> FileDialog(owner)
    else -> FileDialog(null as Frame?)
  }
  dialog.mode = FileDialog.LOAD
  dialog.isMultipleMode = true
  if (filter != null) dialog.filenameFilter = filter
  try {
    dialog.isVisible = true
    return dialog.files.orEmpty().toList()
  } finally {
    dialog.dispose()
  }
}
