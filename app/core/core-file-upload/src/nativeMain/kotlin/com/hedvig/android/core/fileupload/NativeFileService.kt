package com.hedvig.android.core.fileupload

import com.eygraber.uri.Uri
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class NativeFileService : FileService {
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
