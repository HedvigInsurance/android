package com.hedvig.feature.claim.chat.data.file

/**
 * Expected interface to abstract platform-specific file handling.
 */
expect class AudioFileReference {
  val pathOrUri: String
  suspend fun readBytes(): ByteArray
}
