package com.hedvig.android.core.fileupload

import kotlinx.io.IOException

class BackendFileLimitException(message: String) : IOException(message) {
  constructor(uri: String, limit: Long) :
    this("Failed to upload with uri:$uri. Content size above backend limit:$limit")
}

internal const val BACKEND_CONTENT_SIZE_LIMIT = 512 * 1024 * 1024L // 512 MB
