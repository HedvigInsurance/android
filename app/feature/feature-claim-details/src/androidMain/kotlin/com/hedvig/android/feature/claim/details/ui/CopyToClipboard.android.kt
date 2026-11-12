package com.hedvig.android.feature.claim.details.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService

@Composable
internal actual fun rememberCopyToClipboard(): (text: String, confirmation: String) -> Unit {
  val context = LocalContext.current
  return remember(context) {
    { text, confirmation -> context.copyToClipboardAndShowToast(text, confirmation) }
  }
}

private fun Context.copyToClipboardAndShowToast(text: String, toast: String) {
  getSystemService<ClipboardManager>()?.setPrimaryClip(ClipData.newPlainText(null, text))
  Toast.makeText(this, toast, Toast.LENGTH_SHORT).show()
}
