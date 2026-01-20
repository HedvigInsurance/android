package com.hedvig.feature.claim.chat.ui

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri

@Composable
internal actual fun rememberFilePicker(onResult: (Uri?) -> Unit): FilePicker {
  return object : FilePicker {
    override fun launch() {
      // TODO: Implement iOS file picker using UIDocumentPickerViewController
      onResult(null)
    }
  }
}
