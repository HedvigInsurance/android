package com.hedvig.android.feature.chat.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri
import com.eygraber.uri.toKmpUri
import com.hedvig.android.compose.photo.capture.state.ResultLauncher

@Composable
internal actual fun rememberChatVisualMediaPicker(onResult: (List<Uri>) -> Unit): ResultLauncher {
  val photoPicker = rememberLauncherForActivityResult(
    contract = PickMultipleVisualMediaPermittingPersistentAccess(),
  ) { resultingUriList ->
    onResult(resultingUriList.map { it.toKmpUri() })
  }
  return ResultLauncher { photoPicker.launch(PickVisualMediaRequest()) }
}

@Composable
internal actual fun rememberChatFilePicker(onResult: (List<Uri>) -> Unit): ResultLauncher {
  val filePicker = rememberLauncherForActivityResult(
    contract = GetMultipleContentsPermittingPersistentAccess(),
  ) { resultingUriList ->
    onResult(resultingUriList.map { it.toKmpUri() })
  }
  return ResultLauncher { filePicker.launch("*/*") }
}
