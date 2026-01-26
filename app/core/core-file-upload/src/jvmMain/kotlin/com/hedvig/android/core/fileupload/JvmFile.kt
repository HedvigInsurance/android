package com.hedvig.android.core.fileupload

import kotlinx.io.Source

class JvmFile(
  override val fileName: String,
  override val mimeType: String,
) : CommonFile {
  override fun source(): Source {
    TODO("JVM file upload not yet implemented")
  }

  override fun getSize(): Long {
    TODO("JVM file upload not yet implemented")
  }
}
