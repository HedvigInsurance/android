package com.hedvig.android.core.fileupload

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage

internal class JvmDownloadPdfUseCaseImpl : DownloadPdfUseCase {
  override suspend fun invoke(url: String): Either<ErrorMessage, DownloadedFile> {
    TODO("JVM PDF download not yet implemented")
  }
}
