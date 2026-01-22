package com.hedvig.android.core.fileupload

import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

val fileUploadModule = module {
  includes(fileUploadPlatformModule)

  single<FileUploadService> {
    FileUploadService(
      client = get<HttpClient>(),
      fileService = get<FileService>(),
    )
  }

  single<ClaimsServiceUploadFileUseCase> {
    ClaimsServiceUploadFileUseCaseImpl(
      fileUploadService = get<FileUploadService>(),
      buildConstants = get<HedvigBuildConstants>(),
      fileService = get<FileService>(),
    )
  }
}

expect val fileUploadPlatformModule: Module
