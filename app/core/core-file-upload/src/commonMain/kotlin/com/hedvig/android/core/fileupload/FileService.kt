package com.hedvig.android.core.fileupload

import com.eygraber.uri.Uri

/**
 * Platform-agnostic file service for file metadata operations.
 */
interface FileService {
  /**
   * Converts a URI to a CommonFile that can be uploaded.
   */
  fun convertToCommonFile(uri: Uri): CommonFile

  /**
   * Returns the file name from a URI, or null if it cannot be determined.
   */
  fun getFileName(uri: Uri): String?

  /**
   * Returns the MIME type for a given URI.
   */
  fun getMimeType(uri: Uri): String

  /**
   * Checks if file size is within backend limits (512 MB).
   */
  fun isFileSizeWithinBackendLimits(uri: Uri): Boolean
}
