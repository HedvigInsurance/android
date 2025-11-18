package com.hedvig.feature.claim.chat.data.file

import kotlinx.io.Source

internal expect fun AudioFileReference(fileName: String, pathOrUri: String): AudioFileReference

internal interface AudioFileReference {
  val pathOrUri: String
  val fileName: String
  fun source(): Source
  fun readBytes(): ByteArray
}
