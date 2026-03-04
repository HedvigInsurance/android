package com.hedvig.feature.claim.chat.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.fileupload.FileService
import com.hedvig.android.core.fileupload.FileUploadService
import com.hedvig.android.language.LanguageService
import com.hedvig.feature.claim.chat.ClaimChatViewModel
import com.hedvig.feature.claim.chat.data.AudioRecordingManager
import com.hedvig.feature.claim.chat.data.FormFieldSearchUseCase
import com.hedvig.feature.claim.chat.data.FormFieldSearchUseCaseImpl
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
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val claimChatModule = module {
  includes(claimChatPlatformModule)
  viewModel<ClaimChatViewModel> { (developmentFlow: Boolean) ->
    ClaimChatViewModel(
      developmentFlow,
      get<StartClaimIntentUseCase>(),
      get<GetClaimIntentUseCase>(),
      get<SubmitTaskUseCase>(),
      get<SubmitAudioRecordingUseCase>(),
      get<SubmitFileUploadUseCase>(),
      get<SubmitFormUseCase>(),
      get<SubmitSelectUseCase>(),
      get<SubmitSummaryUseCase>(),
      get<AudioRecordingManager>(),
      get<SkipStepUseCase>(),
      get<RegretStepUseCase>(),
      get<FileService>(),
    )
  }

  single<SkipStepUseCase> {
    SkipStepUseCaseImpl(get<ApolloClient>(), get<LanguageService>())
  }

  single<FormFieldSearchUseCase> {
    FormFieldSearchUseCaseImpl(
      get<ApolloClient>()
    )
  }

  single<StartClaimIntentUseCase> {
    StartClaimIntentUseCase(get<ApolloClient>(), get<LanguageService>())
  }

  single<GetClaimIntentUseCase> {
    GetClaimIntentUseCase(get<ApolloClient>(), get<LanguageService>())
  }

  single<SubmitTaskUseCase> {
    SubmitTaskUseCase(get<ApolloClient>(), get<LanguageService>())
  }

  single<SubmitAudioRecordingUseCase> {
    SubmitAudioRecordingUseCase(get<ApolloClient>(), get<UploadFileUseCase>(), get<LanguageService>())
  }

  single<SubmitFileUploadUseCase> {
    SubmitFileUploadUseCase(get<ApolloClient>(), get<UploadFileUseCase>(), get<FileService>(), get<LanguageService>())
  }

  single<SubmitFormUseCase> {
    SubmitFormUseCase(get<ApolloClient>(), get<LanguageService>())
  }

  single<SubmitSelectUseCase> {
    SubmitSelectUseCase(get<ApolloClient>(), get<LanguageService>())
  }

  single<SubmitSummaryUseCase> {
    SubmitSummaryUseCase(get<ApolloClient>(), get<LanguageService>())
  }

  single<UploadFileUseCase> {
    UploadFileUseCase(get<FileUploadService>(), get<HedvigBuildConstants>())
  }

  single<RegretStepUseCase> {
    RegretStepUseCaseImpl(get<ApolloClient>(), get<LanguageService>())
  }
}

expect val claimChatPlatformModule: Module
