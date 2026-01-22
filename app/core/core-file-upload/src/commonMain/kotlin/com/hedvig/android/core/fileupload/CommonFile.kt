package com.hedvig.android.core.fileupload

import kotlinx.io.Source

/**
 * Platform-agnostic representation of a file for upload.
 * Implementations provide platform-specific ways to access file data.
 */
interface CommonFile {
  val fileName: String
  val mimeType: String

  /**
   * Returns a Source for streaming the file contents.
   * This Source should be buffered and ready for reading.
   */
  fun source(): Source

  /**
   * Returns the file size in bytes, or -1 if unknown.
   */
  fun getSize(): Long
}
