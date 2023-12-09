package com.hedvig.android.apollo.auth.listeners.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.auth.listeners.UploadLanguagePreferenceToBackendAuthListener
import com.hedvig.android.apollo.auth.listeners.UploadLanguagePreferenceToBackendUseCase
import com.hedvig.android.apollo.auth.listeners.UploadLanguagePreferenceToBackendUseCaseImpl
import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.language.LanguageService
import org.koin.dsl.bind
import org.koin.dsl.module

val languageAuthListenersModule = module {
  single<UploadLanguagePreferenceToBackendUseCase> {
    UploadLanguagePreferenceToBackendUseCaseImpl(get<ApolloClient>(), get<LanguageService>())
  }
  single<UploadLanguagePreferenceToBackendAuthListener> {
    UploadLanguagePreferenceToBackendAuthListener(get<UploadLanguagePreferenceToBackendUseCase>())
  } bind AuthEventListener::class
}
