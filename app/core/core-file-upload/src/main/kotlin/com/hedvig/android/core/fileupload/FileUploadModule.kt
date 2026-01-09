package com.hedvig.android.core.fileupload

import android.content.Context
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.baseHttpClientQualifier
import io.ktor.client.HttpClient
import kotlin.time.Clock
import org.koin.dsl.module

val fileUploadModule = module {
  single<FileService> { FileService(get<Context>().contentResolver) }
  single<UploadFileService> {
    UploadFileService(
      client = get<HttpClient>(),
      buildConstants = get<HedvigBuildConstants>(),
      contentResolver = get<Context>().contentResolver,
      fileService = get<FileService>(),
    )
  }
  single<UploadFileUseCase> {
    UploadFileUseCaseImpl(
      uploadFileService = get<UploadFileService>(),
      fileService = get<FileService>(),
    )
  }
  single<DownloadPdfUseCase> {
    DownloadPdfUseCaseImpl(
      get<Context>(),
      get<Clock>(),
      get<HttpClient>(baseHttpClientQualifier),
    )
  }
}
