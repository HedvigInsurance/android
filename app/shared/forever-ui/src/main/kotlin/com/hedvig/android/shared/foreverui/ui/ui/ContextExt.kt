package com.hedvig.android.shared.foreverui.ui.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService

internal fun Context.copyToClipboard(text: String) {
  getSystemService<ClipboardManager>()?.setPrimaryClip(ClipData.newPlainText(null, text))
}

internal fun Context.showShareSheet(title: String, configureClosure: ((Intent) -> Unit)?) {
  val intent = Intent().apply {
    action = Intent.ACTION_SEND
  }

  configureClosure?.let { it(intent) }
  startActivity(
    Intent.createChooser(intent, title),
  )
}
