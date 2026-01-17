package com.hedvig.android.compose.photo.capture.state

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri

fun interface ResultLauncher {
  fun launch()
}

@Composable
expect fun rememberPickMultipleVisualMediaResultLauncher(block: (uris: List<Uri>) -> Unit): ResultLauncher

@Composable
expect fun rememberGetMultipleContentsResultLauncher(block: (uris: List<Uri>) -> Unit): ResultLauncher
