package com.hedvig.android.compose.photo.capture.state

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
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
class PhotoCaptureState internal constructor(
  initialPhotoPath: String?,
  private val context: Context,
  private val externalPhotosDirectory: File?,
  private val authority: String,
) {
  internal var currentPhotoPath by mutableStateOf<String?>(initialPhotoPath)
    private set

  internal var launcher: ActivityResultLauncher<Uri>? = null

  fun launchTakePhotoRequest() {
    val newPhotoFile: File = createFileInExternalPhotosDirectory(
      "JPEG_${System.currentTimeMillis()}.jpg",
    ).apply {
      currentPhotoPath = absolutePath
    }
    val newPhotoUri: Uri = FileProvider.getUriForFile(context, authority, newPhotoFile)
    launcher?.launch(newPhotoUri) ?: throw IllegalStateException("ActivityResultLauncher cannot be null")
  }

  internal fun clearCurrentPhotoPath() {
    currentPhotoPath = null
  }

  private fun createFileInExternalPhotosDirectory(fileName: String): File {
    return File(
      externalPhotosDirectory,
      fileName,
    )
  }

  companion object {
    @Suppress("ktlint:standard:function-naming")
    fun Saver(context: Context, externalPhotosDirectory: File?, authority: String): Saver<PhotoCaptureState, *> = Saver(
      save = { it.currentPhotoPath },
      restore = { PhotoCaptureState(it, context, externalPhotosDirectory, authority) },
    )
  }
}

@Composable
fun rememberPhotoCaptureState(appPackageId: String, onPhotoCaptured: (uri: Uri) -> Unit): PhotoCaptureState {
  val context = LocalContext.current
  val activity = context.findActivity()
  val externalPhotosDirectory = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
  val authority = "$appPackageId.provider"
  val photoCaptureState = rememberSaveable(
    saver = PhotoCaptureState.Saver(context, externalPhotosDirectory, authority),
  ) {
    PhotoCaptureState(null, context, externalPhotosDirectory, authority)
  }

  val takePictureLauncher: ActivityResultLauncher<Uri> = rememberLauncherForActivityResult(
    contract = TakePicturePermittingPersistentAccess(),
  ) { didSucceed ->
    logcat {
      "Take picture launcher result, didSucceed:$didSucceed, currentPhotoPath:${photoCaptureState.currentPhotoPath}"
    }
    val tempFilePath = photoCaptureState.currentPhotoPath
    if (tempFilePath == null) {
      logcat(LogPriority.ERROR) {
        "Finished taking picture, but currentPhotoPath was null. Failed to properly store the path to it."
      }
    } else if (didSucceed) {
      val fileUri = Uri.fromFile(File(tempFilePath))
      logcat { "Captured picture with fileUri:$fileUri" }
      onPhotoCaptured(fileUri)
      photoCaptureState.clearCurrentPhotoPath()
    } else {
      logcat(LogPriority.INFO) { "Did not finish taking a picture, cancelled request normally" }
    }
  }
  DisposableEffect(photoCaptureState, takePictureLauncher) {
    photoCaptureState.launcher = takePictureLauncher
    onDispose {
      photoCaptureState.launcher = null
    }
  }

  return photoCaptureState
}

private fun Context.findActivity(): Activity {
  var context = this
  while (context is ContextWrapper) {
    if (context is Activity) return context
    context = context.baseContext
  }
  throw IllegalStateException("no activity")
}

private class TakePicturePermittingPersistentAccess : ActivityResultContracts.TakePicture() {
  override fun createIntent(context: Context, input: Uri): Intent {
    return super.createIntent(context, input).apply {
      addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
  }
}
