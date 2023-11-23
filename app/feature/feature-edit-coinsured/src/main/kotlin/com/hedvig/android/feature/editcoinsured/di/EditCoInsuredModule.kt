package com.hedvig.android.feature.editcoinsured.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCaseImpl
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val editCoInsuredModule = module {
  single<GetCoInsuredUseCaseImpl> {
    GetCoInsuredUseCaseImpl(
      get<ApolloClient>(octopusClient),
    )
  }

  single<GetCoInsuredUseCaseProvider> {
    GetCoInsuredUseCaseProvider(
      demoManager = get<DemoManager>(),
      prodImpl = get<GetCoInsuredUseCaseImpl>(),
      demoImpl = get<GetCoInsuredUseCaseImpl>(),
    )
  }

  viewModel<EditCoInsuredViewModel> { (contractId: String) ->
    EditCoInsuredViewModel(
      contractId,
      get<GetCoInsuredUseCaseProvider>(),
    )
  }
}
