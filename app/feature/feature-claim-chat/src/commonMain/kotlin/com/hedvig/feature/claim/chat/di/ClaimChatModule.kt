package com.hedvig.feature.claim.chat.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.feature.claim.chat.ClaimChatViewModel
import com.hedvig.feature.claim.chat.data.GetClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.RegretStepUseCase
import com.hedvig.feature.claim.chat.data.RegretStepUseCaseImpl
import com.hedvig.feature.claim.chat.data.SkipStepUseCase
import com.hedvig.feature.claim.chat.data.SkipStepUseCaseImpl
import com.hedvig.feature.claim.chat.data.StartClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.SubmitAudioRecordingUseCase
import com.hedvig.feature.claim.chat.data.SubmitFileUploadUseCase
import com.hedvig.feature.claim.chat.data.SubmitFormUseCase
import com.hedvig.feature.claim.chat.data.SubmitSelectUseCase
import com.hedvig.feature.claim.chat.data.SubmitSummaryUseCase
import com.hedvig.feature.claim.chat.data.SubmitTaskUseCase
import com.hedvig.feature.claim.chat.data.UploadFileUseCase
import com.hedvig.feature.claim.chat.data.file.FileService
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val claimChatModule = module {
  includes(claimChatPlatformModule)
  viewModel<ClaimChatViewModel> { (developmentFlow: Boolean) ->
    ClaimChatViewModel(
      developmentFlow,
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get<SkipStepUseCase>(),
      get<RegretStepUseCase>(),
      get<FileService>(),
    )
  }

  single<SkipStepUseCase> {
    SkipStepUseCaseImpl(get<ApolloClient>(), get())
  }

  single<StartClaimIntentUseCase> {
    StartClaimIntentUseCase(get<ApolloClient>(), get())
  }

  single<GetClaimIntentUseCase> {
    GetClaimIntentUseCase(get<ApolloClient>(), get())
  }

  single<SubmitTaskUseCase> {
    SubmitTaskUseCase(get<ApolloClient>(), get())
  }

  single<SubmitAudioRecordingUseCase> {
    SubmitAudioRecordingUseCase(get<ApolloClient>(), get(), get())
  }

  single<SubmitFileUploadUseCase> {
    SubmitFileUploadUseCase(get<ApolloClient>(), get(), get(), get())
  }

  single<SubmitFormUseCase> {
    SubmitFormUseCase(get<ApolloClient>(), get())
  }

  single<SubmitSelectUseCase> {
    SubmitSelectUseCase(get<ApolloClient>(), get())
  }

  single<SubmitSummaryUseCase> {
    SubmitSummaryUseCase(get<ApolloClient>(), get())
  }

  single<UploadFileUseCase> {
    UploadFileUseCase(get<HttpClient>())
  }

  single<RegretStepUseCase> {
    RegretStepUseCaseImpl(get<ApolloClient>(), get())
  }
}

expect val claimChatPlatformModule: Module
