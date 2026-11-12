package com.hedvig.android.compose.photo.capture.state

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri

interface PhotoCaptureState {
  /**
   * Whether this platform has a camera flow to take a photo with. Offer the option only when it is true.
   */
  val isAvailable: Boolean

  fun launchTakePhotoRequest()

  companion object
}

@Composable
expect fun rememberPhotoCaptureState(appPackageId: String, onPhotoCaptured: (uri: Uri) -> Unit): PhotoCaptureState
