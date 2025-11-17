package com.hedvig.feature.claim.chat.data.file

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal actual fun AudioFileReference(pathOrUri: String): AudioFileReference = object : AudioFileReference{
  override val pathOrUri: String = pathOrUri

  override suspend fun readBytes(): ByteArray {
    return withContext(Dispatchers.IO) {
      File(pathOrUri).readBytes()
    }
  }
}
