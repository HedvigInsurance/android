package com.hedvig.feature.claim.chat.data.file

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class AudioFileReference actual constructor(val pathOrUri: String) {
  actual suspend fun readBytes(): ByteArray {
    return withContext(Dispatchers.IO) {
      File(pathOrUri).readBytes()
    }
  }
}
