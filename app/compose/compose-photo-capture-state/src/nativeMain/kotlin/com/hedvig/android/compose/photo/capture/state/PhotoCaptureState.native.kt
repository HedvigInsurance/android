package com.hedvig.android.compose.photo.capture.state

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri

// todo ios
@Composable
actual fun rememberPhotoCaptureState(appPackageId: String, onPhotoCaptured: (uri: Uri) -> Unit): PhotoCaptureState {
  return object : PhotoCaptureState {
    override fun launchTakePhotoRequest() {
      TODO("Not yet implemented")
    }
  }
}
