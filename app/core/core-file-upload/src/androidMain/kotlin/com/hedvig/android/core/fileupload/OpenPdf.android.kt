package com.hedvig.android.core.fileupload

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.hedvig.core.common.android.sharePDF
import java.io.File

@Composable
actual fun rememberOpenPdf(applicationId: String): (DownloadedFile) -> Unit {
  val context = LocalContext.current
  return remember(context, applicationId) {
    { downloadedFile -> context.sharePDF(File(downloadedFile.path), applicationId) }
  }
}
