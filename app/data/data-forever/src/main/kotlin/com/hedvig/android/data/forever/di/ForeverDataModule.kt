package com.hedvig.android.data.forever.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.data.forever.ForeverRepositoryDemo
import com.hedvig.android.data.forever.ForeverRepositoryImpl
import com.hedvig.android.language.LanguageService
import org.koin.dsl.module

val foreverDataModule = module {
  single<ForeverRepositoryImpl> {
    ForeverRepositoryImpl(
      apolloClient = get<ApolloClient>(giraffeClient),
      languageService = get<LanguageService>(),
    )
  }
  single<ForeverRepositoryDemo> {
    ForeverRepositoryDemo()
  }
  single {
    ForeverRepositoryProvider(
      demoManager = get(),
      prodImpl = get<ForeverRepositoryImpl>(),
      demoImpl = get<ForeverRepositoryDemo>(),
    )
  }
}

