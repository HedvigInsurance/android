package com.hedvig.feature.claim.chat.data.file

internal expect fun AudioFileReference(pathOrUri: String): AudioFileReference

internal interface AudioFileReference {
  val pathOrUri: String

  suspend fun readBytes(): ByteArray
}
