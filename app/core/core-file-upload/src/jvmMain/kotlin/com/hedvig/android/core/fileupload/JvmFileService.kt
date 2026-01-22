package com.hedvig.android.core.fileupload

import com.eygraber.uri.Uri

class JvmFileService : FileService {
  override fun convertToCommonFile(uri: Uri): CommonFile {
    return JvmFile(
      fileName = "todo",
      mimeType = "todo",
    )
  }

  override fun getFileName(uri: Uri): String? {
    TODO("JVM file operations not yet implemented")
  }

  override fun getMimeType(uri: Uri): String {
    TODO("JVM file operations not yet implemented")
  }

  override fun isFileSizeWithinBackendLimits(uri: Uri): Boolean {
    TODO("JVM file operations not yet implemented")
  }
}
