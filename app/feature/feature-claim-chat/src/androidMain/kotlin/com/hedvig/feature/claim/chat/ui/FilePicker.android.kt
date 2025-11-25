package com.hedvig.feature.claim.chat.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.eygraber.uri.Uri
import com.eygraber.uri.toKmpUri

@Composable
internal actual fun rememberFilePicker(onResult: (Uri?) -> Unit): FilePicker {
  val updatedOnResult by rememberUpdatedState(onResult)
  val launcher = rememberLauncherForActivityResult(GetMultipleContentsPermittingPersistentAccess()) { uri ->
    updatedOnResult(uri?.toKmpUri())
  }
  val filePicker = remember {
    object : FilePicker {
      override fun launch() {
        launcher.launch("*/*")
      }
    }
  }
  return filePicker
}

internal class GetMultipleContentsPermittingPersistentAccess : ActivityResultContracts.GetContent() {
  override fun createIntent(context: Context, input: String): Intent {
    return super.createIntent(context, input).apply {
      addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
  }
}
