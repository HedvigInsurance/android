package com.hedvig.android.compose.photo.capture.state

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.eygraber.uri.toKmpUri

@Composable
actual fun rememberPickMultipleVisualMediaResultLauncher(
  block: (uris: List<com.eygraber.uri.Uri>) -> Unit,
): ResultLauncher {
  val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(),
  ) { resultingUriList: List<Uri> ->
    block(resultingUriList.map { it.toKmpUri() })
  }
  return ResultLauncher {
    launcher.launch(PickVisualMediaRequest())
  }
}

@Composable
actual fun rememberGetMultipleContentsResultLauncher(
  block: (uris: List<com.eygraber.uri.Uri>) -> Unit,
): ResultLauncher {
  val filePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetMultipleContents(),
  ) { resultingUriList: List<Uri> ->
    block(resultingUriList.map { it.toKmpUri() })
  }
  return ResultLauncher {
    filePicker.launch("*/*")
  }
}
