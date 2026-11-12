package com.hedvig.android.core.fileupload

import java.nio.file.Files
import java.nio.file.Path
import kotlinx.io.IOException
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered

class JvmFile(
  override val fileName: String,
  override val mimeType: String,
  private val path: Path?,
) : CommonFile {
  override fun source(): Source {
    if (path == null || !Files.isRegularFile(path)) {
      throw IOException("No readable file at:$path")
    }
    return Files.newInputStream(path).asSource().buffered()
  }

  override fun getSize(): Long {
    return sizeOf(path)
  }
}
