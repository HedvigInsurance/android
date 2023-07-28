package com.hedvig.android.feature.forever

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService

fun Context.copyToClipboard(
  text: String,
) {
  getSystemService<ClipboardManager>()?.setPrimaryClip(ClipData.newPlainText(null, text))
}

fun Context.showShareSheet(title: String, configureClosure: ((Intent) -> Unit)?) {
  val intent = Intent().apply {
    action = Intent.ACTION_SEND
  }

  configureClosure?.let { it(intent) }
  startActivity(
    Intent.createChooser(intent, title),
  )
}
