package com.hedvig.feature.claim.chat.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.feature.claim.chat.ClaimChatViewModel
import com.hedvig.feature.claim.chat.data.GetClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.StartClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.SubmitAudioRecordingUseCase
import com.hedvig.feature.claim.chat.data.SubmitFormUseCase
import com.hedvig.feature.claim.chat.data.SubmitSummaryUseCase
import com.hedvig.feature.claim.chat.data.SubmitTaskUseCase
import com.hedvig.feature.claim.chat.data.UploadAudioUseCase
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val claimChatModule = module {
  viewModel<ClaimChatViewModel> { (isDevFlow: Boolean, messageId: String?) ->
    ClaimChatViewModel(
      messageId = messageId,
      isDevelopmentFlow = isDevFlow,
      startClaimIntentUseCase = get<StartClaimIntentUseCase>(),
      getClaimIntentUseCase = get<GetClaimIntentUseCase>(),
      submitAudioRecordingUseCase = get<SubmitAudioRecordingUseCase>(),
      uploadAudioUseCase = get<UploadAudioUseCase>(),
      submitFormUseCase = get<SubmitFormUseCase>(),
      submitTaskUseCase = get<SubmitTaskUseCase>(),
      submitSummaryUseCase = get<SubmitSummaryUseCase>(),
    )
  }

  single<StartClaimIntentUseCase> {
    StartClaimIntentUseCase(get<ApolloClient>())
  }

  single<GetClaimIntentUseCase> {
    GetClaimIntentUseCase(get<ApolloClient>())
  }

  single<SubmitAudioRecordingUseCase> {
    SubmitAudioRecordingUseCase(get<ApolloClient>())
  }

  single<HttpClient> {
    HttpClient()
  }

  single<UploadAudioUseCase> {
    UploadAudioUseCase(get<HttpClient>())
  }

  single<SubmitFormUseCase> {
    SubmitFormUseCase(get<ApolloClient>())
  }

  single<SubmitTaskUseCase> {
    SubmitTaskUseCase(get<ApolloClient>())
  }

  single<SubmitSummaryUseCase> {
    SubmitSummaryUseCase(get<ApolloClient>())
  }
}
