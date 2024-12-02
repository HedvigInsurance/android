package com.hedvig.android.core.fileupload

import android.content.Context
import arrow.retrofit.adapter.either.EitherCallAdapterFactory
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val fileUploadModule = module {
  single<FileService> { FileService(get<Context>().contentResolver) }
  single<UploadFileService> {
    Retrofit.Builder()
      .callFactory(get<OkHttpClient>())
      .baseUrl("${get<HedvigBuildConstants>().urlClaimsService}/api/")
      .addCallAdapterFactory(EitherCallAdapterFactory.create())
      .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
      .build()
      .create(UploadFileService::class.java)
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
      get<OkHttpClient.Builder>().build(),
    )
  }
}
