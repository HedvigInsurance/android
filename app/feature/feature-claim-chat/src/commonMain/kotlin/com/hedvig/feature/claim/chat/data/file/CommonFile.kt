package com.hedvig.feature.claim.chat.data.file

import kotlinx.io.Source

internal interface CommonFile {
  val fileName: String
  val description: String

  fun source(): Source

  fun readBytes(): ByteArray
}
