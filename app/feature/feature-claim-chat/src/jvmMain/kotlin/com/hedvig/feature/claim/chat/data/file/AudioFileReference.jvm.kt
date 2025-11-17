package com.hedvig.feature.claim.chat.data.file

internal actual fun AudioFileReference(pathOrUri: String): AudioFileReference = object : AudioFileReference{
  override val pathOrUri: String = pathOrUri

  override suspend fun readBytes(): ByteArray {
    return ByteArray(0)
  }
}
