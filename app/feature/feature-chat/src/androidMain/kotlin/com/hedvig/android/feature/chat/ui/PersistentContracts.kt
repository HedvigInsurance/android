package com.hedvig.android.feature.chat.ui

import android.content.Context
import android.content.Intent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

internal class PickMultipleVisualMediaPermittingPersistentAccess : ActivityResultContracts.PickMultipleVisualMedia() {
  override fun createIntent(context: Context, input: PickVisualMediaRequest): Intent {
    return super.createIntent(context, input).apply {
      addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
  }
}

internal class GetMultipleContentsPermittingPersistentAccess : ActivityResultContracts.GetMultipleContents() {
  override fun createIntent(context: Context, input: String): Intent {
    return super.createIntent(context, input).apply {
      addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
  }
}
