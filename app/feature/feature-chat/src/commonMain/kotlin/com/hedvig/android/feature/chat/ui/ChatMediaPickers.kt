package com.hedvig.android.feature.chat.ui

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri
import com.hedvig.android.compose.photo.capture.state.ResultLauncher

/** Lets the member pick photos and videos to send. */
@Composable
internal expect fun rememberChatVisualMediaPicker(onResult: (List<Uri>) -> Unit): ResultLauncher

/** Lets the member pick files of any type to send. */
@Composable
internal expect fun rememberChatFilePicker(onResult: (List<Uri>) -> Unit): ResultLauncher
