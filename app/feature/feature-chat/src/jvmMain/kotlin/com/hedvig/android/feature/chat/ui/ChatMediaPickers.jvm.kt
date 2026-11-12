package com.hedvig.android.feature.chat.ui

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri
import com.hedvig.android.compose.photo.capture.state.ResultLauncher
import com.hedvig.android.compose.photo.capture.state.rememberGetMultipleContentsResultLauncher
import com.hedvig.android.compose.photo.capture.state.rememberPickMultipleVisualMediaResultLauncher

@Composable
internal actual fun rememberChatVisualMediaPicker(onResult: (List<Uri>) -> Unit): ResultLauncher =
  rememberPickMultipleVisualMediaResultLauncher(onResult)

@Composable
internal actual fun rememberChatFilePicker(onResult: (List<Uri>) -> Unit): ResultLauncher =
  rememberGetMultipleContentsResultLauncher(onResult)
