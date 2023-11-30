package com.hedvig.android.feature.editcoinsured.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.feature.editcoinsured.data.CommitMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.CommitMidtermChangeUseCaseImpl
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeUseCaseImpl
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCase
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCaseImpl
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCase
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCaseImpl
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val editCoInsuredModule = module {
  single<GetCoInsuredUseCase> {
    GetCoInsuredUseCaseImpl(
      get<ApolloClient>(octopusClient),
    )
  }

  single<FetchCoInsuredPersonalInformationUseCase> {
    FetchCoInsuredPersonalInformationUseCaseImpl(
      get<ApolloClient>(octopusClient),
    )
  }

  single<CreateMidtermChangeUseCase> {
    CreateMidtermChangeUseCaseImpl(
      get<ApolloClient>(octopusClient),
    )
  }

  single<CommitMidtermChangeUseCase> {
    CommitMidtermChangeUseCaseImpl(
      get<ApolloClient>(octopusClient),
    )
  }

  viewModel<EditCoInsuredViewModel> { (contractId: String) ->
    EditCoInsuredViewModel(
      contractId,
      get<GetCoInsuredUseCase>(),
      get<FetchCoInsuredPersonalInformationUseCase>(),
      get<CreateMidtermChangeUseCase>(),
      get<CommitMidtermChangeUseCase>(),
      get<NetworkCacheManager>(),
    )
  }
}
