package com.hedvig.android.core.common.android

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

fun Context.sharePDF(file: File, applicationId: String) {
  val contentUri = FileProvider.getUriForFile(this, "$applicationId.provider", file)

  val sendIntent: Intent = Intent().apply {
    action = Intent.ACTION_VIEW
    setDataAndType(contentUri, "application/pdf")
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
  }
  val shareIntent = Intent.createChooser(sendIntent, "Hedvig PDF")
  startActivity(shareIntent)
}
