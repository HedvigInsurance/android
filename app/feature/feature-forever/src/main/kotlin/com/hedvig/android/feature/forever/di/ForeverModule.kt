package com.hedvig.android.feature.forever.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.forever.ForeverViewModel
import com.hedvig.android.feature.forever.data.ForeverRepositoryDemo
import com.hedvig.android.feature.forever.data.ForeverRepositoryImpl
import com.hedvig.android.feature.forever.data.ForeverRepositoryProvider
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val foreverModule = module {
  viewModel<ForeverViewModel> {
    ForeverViewModel(
      get<ForeverRepositoryProvider>(),
    )
  }
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
