package com.hedvig.android.core.fileupload

import com.eygraber.uri.Uri

class NativeFileService : FileService {
  override fun convertToCommonFile(uri: Uri): CommonFile {
    return NativeFile(
      fileName = "todo",
      mimeType = "todo",
    )
  }

  override fun getFileName(uri: Uri): String? {
    TODO("Native file operations not yet implemented")
  }

  override fun getMimeType(uri: Uri): String {
    TODO("Native file operations not yet implemented")
  }

  override fun isFileSizeWithinBackendLimits(uri: Uri): Boolean {
    TODO("Native file operations not yet implemented")
  }
}
