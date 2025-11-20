package com.hedvig.feature.claim.chat.ui

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri

interface FilePicker {
  fun launch()
}

@Composable
internal expect fun rememberFilePicker(onResult: (Uri?) -> Unit): FilePicker
