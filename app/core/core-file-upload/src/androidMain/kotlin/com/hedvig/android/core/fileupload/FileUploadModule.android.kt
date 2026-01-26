package com.hedvig.android.core.fileupload

import android.content.Context
import com.hedvig.android.core.common.di.baseHttpClientQualifier
import io.ktor.client.HttpClient
import kotlin.time.Clock
import org.koin.core.module.Module
import org.koin.dsl.module

actual val fileUploadPlatformModule: Module = module {
  single<FileService> {
    AndroidFileService(
      contentResolver = get<Context>().contentResolver,
    )
  }

  single<DownloadPdfUseCase> {
    AndroidDownloadPdfUseCaseImpl(
      context = get<Context>(),
      clock = get<Clock>(),
      httpClient = get<HttpClient>(baseHttpClientQualifier),
    )
  }
}
