package com.hedvig.feature.claim.chat.data.file

import kotlinx.io.Source

interface CommonFile {
  val fileName: String
  val description: String
  val mimeType: String

  fun source(): Source

  fun readBytes(): ByteArray
}
