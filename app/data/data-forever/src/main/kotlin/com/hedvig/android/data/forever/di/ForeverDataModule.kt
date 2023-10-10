package com.hedvig.android.data.forever.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.data.forever.ForeverRepositoryDemo
import com.hedvig.android.data.forever.ForeverRepositoryImpl
import com.hedvig.android.language.LanguageService
import org.koin.dsl.module

val foreverDataModule = module {
  single<ForeverRepositoryImpl> {
    ForeverRepositoryImpl(
      apolloClientOctopus = get<ApolloClient>(octopusClient),
      apolloClientGiraffe = get<ApolloClient>(giraffeClient),
      languageService = get<LanguageService>(),
    )
  }
  single<ForeverRepositoryDemo> {
    ForeverRepositoryDemo()
  }
  single {
    ForeverRepositoryProvider(
      demoManager = get<DemoManager>(),
      prodImpl = get<ForeverRepositoryImpl>(),
      demoImpl = get<ForeverRepositoryDemo>(),
    )
  }
}
