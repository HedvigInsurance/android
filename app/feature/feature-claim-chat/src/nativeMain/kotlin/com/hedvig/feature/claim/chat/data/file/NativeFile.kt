package com.hedvig.feature.claim.chat.data.file

import com.eygraber.uri.Uri
import kotlinx.io.Source

internal class NativeFileService : FileService {
  override fun convertToCommonFile(uri: Uri): CommonFile {
    // TODO: Implement iOS file conversion from URI
    return object : CommonFile {
      override val fileName: String = "file"
      override val description: String = "TODO: iOS file"

      override fun source(): Source {
        TODO("iOS file source not yet implemented")
      }

      override fun readBytes(): ByteArray {
        TODO("iOS file read not yet implemented")
      }
    }
  }
}
