package com.hedvig.android.core.fileupload

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage

internal class NativeDownloadPdfUseCaseImpl : DownloadPdfUseCase {
  override suspend fun invoke(url: String): Either<ErrorMessage, DownloadedFile> {
    TODO("Native PDF download not yet implemented")
  }
}
