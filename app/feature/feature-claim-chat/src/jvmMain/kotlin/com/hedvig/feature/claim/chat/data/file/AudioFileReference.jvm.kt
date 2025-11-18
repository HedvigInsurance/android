package com.hedvig.feature.claim.chat.data.file

import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray

internal actual fun AudioFileReference(fileName:String, pathOrUri: String): AudioFileReference = object : AudioFileReference{
  override val pathOrUri: String = pathOrUri
  override val fileName: String = fileName
  private val path: Path = Path(pathOrUri)

  override fun source(): Source {
    return SystemFileSystem.source(path).buffered()
  }

  override fun readBytes(): ByteArray {
    return source().readByteArray()
  }
}
