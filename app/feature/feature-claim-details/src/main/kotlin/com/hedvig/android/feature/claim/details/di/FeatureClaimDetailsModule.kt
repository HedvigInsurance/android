package com.hedvig.android.feature.claim.details.di

import android.content.Context
import arrow.retrofit.adapter.either.EitherCallAdapterFactory
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.android.FileService
import com.hedvig.android.feature.claim.details.data.GetClaimDetailUiStateUseCase
import com.hedvig.android.feature.claim.details.data.UploadFileService
import com.hedvig.android.feature.claim.details.data.UploadFileUseCase
import com.hedvig.android.feature.claim.details.data.UploadFileUseCaseImpl
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsViewModel
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val claimDetailsModule = module {
  single<GetClaimDetailUiStateUseCase> { GetClaimDetailUiStateUseCase(get<ApolloClient>()) }
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
      contentResolver = get<Context>().contentResolver,
    )
  }

  viewModel<ClaimDetailsViewModel> { (claimId: String) ->
    ClaimDetailsViewModel(claimId, get<GetClaimDetailUiStateUseCase>(), get<UploadFileUseCase>())
  }
}
