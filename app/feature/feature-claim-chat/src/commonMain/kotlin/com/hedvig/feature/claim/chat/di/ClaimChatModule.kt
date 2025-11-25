package com.hedvig.feature.claim.chat.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.feature.claim.chat.ClaimChatViewModel
import com.hedvig.feature.claim.chat.data.GetClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.StartClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.SubmitAudioRecordingUseCase
import com.hedvig.feature.claim.chat.data.SubmitFileUploadUseCase
import com.hedvig.feature.claim.chat.data.SubmitFormUseCase
import com.hedvig.feature.claim.chat.data.SubmitSelectUseCase
import com.hedvig.feature.claim.chat.data.SubmitSummaryUseCase
import com.hedvig.feature.claim.chat.data.SubmitTaskUseCase
import com.hedvig.feature.claim.chat.data.UploadFileUseCase
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val claimChatModule = module {
  includes(claimChatPlatformModule)
  viewModel<ClaimChatViewModel> { (sourceMessageId: String?, developmentFlow: Boolean) ->
    ClaimChatViewModel(
      sourceMessageId,
      developmentFlow,
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
    )
  }

  single<StartClaimIntentUseCase> {
    StartClaimIntentUseCase(get<ApolloClient>())
  }

  single<GetClaimIntentUseCase> {
    GetClaimIntentUseCase(get<ApolloClient>())
  }

  single<SubmitTaskUseCase> {
    SubmitTaskUseCase(get<ApolloClient>())
  }

  single<SubmitAudioRecordingUseCase> {
    SubmitAudioRecordingUseCase(get<ApolloClient>(), get())
  }

  single<SubmitFileUploadUseCase> {
    SubmitFileUploadUseCase(get<ApolloClient>(), get(), get())
  }

  single<SubmitFormUseCase> {
    SubmitFormUseCase(get<ApolloClient>())
  }

  single<SubmitSelectUseCase> {
    SubmitSelectUseCase(get<ApolloClient>())
  }

  single<SubmitSummaryUseCase> {
    SubmitSummaryUseCase(get<ApolloClient>())
  }

  single<UploadFileUseCase> {
    UploadFileUseCase(get<HttpClient>())
  }
}

expect val claimChatPlatformModule: Module
