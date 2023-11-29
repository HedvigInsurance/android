package com.hedvig.android.feature.chat.chatfilestate

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import java.io.File

@Stable
internal class ChatFileState internal constructor(
  initialPhotoPath: String?,
  private val context: Context,
  private val externalPhotosDirectory: File?,
  private val authority: String,
) {
  var currentPhotoPath by mutableStateOf<String?>(initialPhotoPath)
    private set

  var launcher: ActivityResultLauncher<Uri>? = null

  fun startTakePicture() {
    val newPhotoFile: File = createFileInExternalPhotosDirectory(
      "JPEG_${System.currentTimeMillis()}.jpg",
    ).apply {
      currentPhotoPath = absolutePath
    }
    val newPhotoUri: Uri = getUriForFile(newPhotoFile)
    launcher?.launch(newPhotoUri) ?: throw IllegalStateException("ActivityResultLauncher cannot be null")
  }

  fun clearCurrentPhotoPath() {
    currentPhotoPath = null
  }

  private fun getUriForFile(file: File): Uri {
    return FileProvider.getUriForFile(
      context,
      authority,
      file,
    )
  }

  private fun createFileInExternalPhotosDirectory(fileName: String): File {
    return File(
      externalPhotosDirectory,
      fileName,
    )
  }

  companion object {
    @Suppress("ktlint:standard:function-naming")
    fun Saver(context: Context, externalPhotosDirectory: File?, authority: String): Saver<ChatFileState, *> = Saver(
      save = { it.currentPhotoPath },
      restore = { ChatFileState(it, context, externalPhotosDirectory, authority) },
    )
  }
}

@Composable
internal fun rememberChatFileState(appPackageId: String, onSendFile: (file: Uri) -> Unit): ChatFileState {
  val context = LocalContext.current
  val activity = context.findActivity()
  val externalPhotosDirectory = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
  val authority = "$appPackageId.provider"
  val chatFileState = rememberSaveable(saver = ChatFileState.Saver(context, externalPhotosDirectory, authority)) {
    ChatFileState(null, context, externalPhotosDirectory, authority)
  }

  val takePictureLauncher: ActivityResultLauncher<Uri> = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture(),
  ) { didSucceed ->
    logcat {
      "Take picture launcher result, didSucceed:$didSucceed, currentPhotoPath:${chatFileState.currentPhotoPath}"
    }
    val tempFilePath = chatFileState.currentPhotoPath
    if (tempFilePath == null) {
      logcat(LogPriority.ERROR) {
        "Finished taking picture, but currentPhotoPath was null. Failed to properly store the path to it."
      }
    } else if (didSucceed) {
      val fileUri = Uri.fromFile(File(tempFilePath))
      logcat { "Sending file with fileUri:$fileUri" }
      onSendFile(fileUri)
      chatFileState.clearCurrentPhotoPath()
    } else {
      logcat(LogPriority.INFO) { "Did not finish taking a picture, cancelled request normally" }
    }
  }
  DisposableEffect(chatFileState, takePictureLauncher) {
    chatFileState.launcher = takePictureLauncher
    onDispose {
      chatFileState.launcher = null
    }
  }

  return chatFileState
}

private fun Context.findActivity(): Activity {
  var context = this
  while (context is ContextWrapper) {
    if (context is Activity) return context
    context = context.baseContext
  }
  throw IllegalStateException("no activity")
}
