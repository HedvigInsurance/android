package com.hedvig.android.compose.photo.capture.state

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri

interface PhotoCaptureState {
  fun launchTakePhotoRequest()

  companion object
}

@Composable
expect fun rememberPhotoCaptureState(appPackageId: String, onPhotoCaptured: (uri: Uri) -> Unit): PhotoCaptureState
