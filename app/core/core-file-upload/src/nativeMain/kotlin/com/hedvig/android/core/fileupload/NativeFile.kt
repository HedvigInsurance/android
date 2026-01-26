package com.hedvig.android.core.fileupload

import kotlinx.io.Source

class NativeFile(
  override val fileName: String,
  override val mimeType: String,
) : CommonFile {
  override fun source(): Source {
    TODO("Native file upload not yet implemented")
  }

  override fun getSize(): Long {
    TODO("Native file upload not yet implemented")
  }
}
