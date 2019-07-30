package com.hedvig.app.feature.chat

import android.net.Uri

data class FileUploadOutcome(
    val uri: Uri,
    val wasSuccessful: Boolean
)
