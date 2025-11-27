package com.hedvig.feature.claim.chat.data.file

import com.eygraber.uri.Uri
import kotlinx.io.Source

internal class NativeFile(
  override val fileName: String,
  override val description: String,
) : CommonFile {
  override fun source(): Source {
    TODO("Not yet implemented")
  }

  override fun readBytes(): ByteArray {
    TODO("Not yet implemented")
  }
}

internal class NativeFileService : FileService {
  override fun convertToCommonFile(uri: Uri): CommonFile {
    return NativeFile("todo", "todo")
  }
}
