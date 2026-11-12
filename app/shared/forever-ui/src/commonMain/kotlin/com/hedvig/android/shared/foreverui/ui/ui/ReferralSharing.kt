package com.hedvig.android.shared.foreverui.ui.ui

import androidx.compose.runtime.Composable

@Composable
internal expect fun rememberShareHandler(): (text: String) -> Unit

@Composable
internal expect fun rememberCopyToClipboard(): (text: String) -> Unit
