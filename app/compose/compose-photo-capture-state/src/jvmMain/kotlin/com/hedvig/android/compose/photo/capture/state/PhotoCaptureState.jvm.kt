package com.hedvig.android.compose.photo.capture.state

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

@Composable
actual fun rememberPhotoCaptureState(appPackageId: String, onPhotoCaptured: (uri: Uri) -> Unit): PhotoCaptureState {
  return CameraUnavailablePhotoCaptureState
}

/** There is no camera to take a photo with, so a request does nothing beyond a warning in the log. */
internal object CameraUnavailablePhotoCaptureState : PhotoCaptureState {
  override fun launchTakePhotoRequest() {
    logcat(LogPriority.WARN) { "Taking a photo is not available on this platform" }
  }
}
