package com.hedvig.android.core.fileupload

import org.koin.core.module.Module
import org.koin.dsl.module

actual val fileUploadPlatformModule: Module = module {
  single<FileService> {
    NativeFileService()
  }

  single<DownloadPdfUseCase> {
    NativeDownloadPdfUseCaseImpl()
  }
}
