package com.hedvig.android.feature.editcoinsured.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeUseCaseImpl
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCaseImpl
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

  single<FetchCoInsuredPersonalInformationUseCaseImpl> {
    FetchCoInsuredPersonalInformationUseCaseImpl(
      get<ApolloClient>(octopusClient),
    )
  }

  single<CreateMidtermChangeUseCase> {
    CreateMidtermChangeUseCaseImpl(
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

  single<FetchCoInsuredPersonalInformationUseCaseProvider> {
    FetchCoInsuredPersonalInformationUseCaseProvider(
      demoManager = get<DemoManager>(),
      prodImpl = get<FetchCoInsuredPersonalInformationUseCaseImpl>(),
      demoImpl = get<FetchCoInsuredPersonalInformationUseCaseImpl>(),
    )
  }

  viewModel<EditCoInsuredViewModel> { (contractId: String) ->
    EditCoInsuredViewModel(
      contractId,
      get<GetCoInsuredUseCaseProvider>(),
      get<FetchCoInsuredPersonalInformationUseCaseProvider>(),
      get<CreateMidtermChangeUseCase>(),
    )
  }

}
