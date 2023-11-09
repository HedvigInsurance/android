package com.hedvig.android.data.forever.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.data.forever.ForeverRepositoryDemo
import com.hedvig.android.data.forever.ForeverRepositoryImpl
import com.hedvig.android.data.forever.ForeverRepositoryProvider
import org.koin.dsl.module

val foreverDataModule = module {
  single<ForeverRepositoryImpl> {
    ForeverRepositoryImpl(apolloClient = get<ApolloClient>(octopusClient))
  }
  single<ForeverRepositoryDemo> {
    ForeverRepositoryDemo()
  }
  single<ForeverRepositoryProvider> {
    ForeverRepositoryProvider(
      demoManager = get<DemoManager>(),
      prodImpl = get<ForeverRepositoryImpl>(),
      demoImpl = get<ForeverRepositoryDemo>(),
    )
  }
}
