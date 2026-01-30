package com.hedvig.android.core.fileupload

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage

/**
 * Platform-agnostic representation of a downloaded file.
 */
data class DownloadedFile(
  val path: String,
  val name: String,
)

interface DownloadPdfUseCase {
  /**
   * Downloads a PDF from the given URL.
   * Returns a platform-specific file path or identifier.
   */
  suspend fun invoke(url: String): Either<ErrorMessage, DownloadedFile>
}
