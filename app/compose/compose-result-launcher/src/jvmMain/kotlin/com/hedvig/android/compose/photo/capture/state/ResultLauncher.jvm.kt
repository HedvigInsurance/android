package com.hedvig.android.compose.photo.capture.state

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri

@Composable
actual fun rememberPickMultipleVisualMediaResultLauncher(block: (uris: List<com.eygraber.uri.Uri>) -> Unit): ResultLauncher {
  return ResultLauncher {
    TODO("Not yet implemented")
  }
}

@Composable
actual fun rememberGetMultipleContentsResultLauncher(block: (uris: List<Uri>) -> Unit): ResultLauncher {
  return ResultLauncher {
    TODO("Not yet implemented")
  }
}
