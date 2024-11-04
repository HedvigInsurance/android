package com.hedvig.android.shared.foreverui.ui.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.shared.foreverui.ui.data.ForeverRepositoryDemo
import com.hedvig.android.shared.foreverui.ui.data.ForeverRepositoryImpl
import com.hedvig.android.shared.foreverui.ui.data.ForeverRepositoryProvider
import com.hedvig.android.shared.foreverui.ui.ui.ForeverViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val foreverModule = module {
  viewModel<ForeverViewModel> {
    ForeverViewModel(
      get<ForeverRepositoryProvider>(),
    )
  }
  single<ForeverRepositoryImpl> {
    ForeverRepositoryImpl(apolloClient = get<ApolloClient>())
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
