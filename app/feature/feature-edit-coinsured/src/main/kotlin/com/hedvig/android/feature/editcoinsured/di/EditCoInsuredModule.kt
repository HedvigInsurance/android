package com.hedvig.android.feature.editcoinsured.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.core.appreview.SelfServiceCompletedEventManager
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.feature.editcoinsured.data.CommitMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.CommitMidtermChangeUseCaseImpl
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeUseCaseImpl
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCase
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCaseImpl
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCase
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCaseImpl
import com.hedvig.android.feature.editcoinsured.data.GetInsurancesForEditCoInsuredUseCase
import com.hedvig.android.feature.editcoinsured.data.GetInsurancesForEditCoInsuredUseCaseImpl
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredViewModel
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val editCoInsuredModule = module {
  single<GetCoInsuredUseCase> {
    GetCoInsuredUseCaseImpl(
      get<ApolloClient>(),
    )
  }

  single<FetchCoInsuredPersonalInformationUseCase> {
    FetchCoInsuredPersonalInformationUseCaseImpl(
      get<ApolloClient>(),
    )
  }

  single<GetInsurancesForEditCoInsuredUseCase> {
    GetInsurancesForEditCoInsuredUseCaseImpl(
      get<ApolloClient>(),
    )
  }

  single<CreateMidtermChangeUseCase> {
    CreateMidtermChangeUseCaseImpl(
      get<ApolloClient>(),
    )
  }

  single<CommitMidtermChangeUseCase> {
    CommitMidtermChangeUseCaseImpl(
      get<ApolloClient>(),
      get<NetworkCacheManager>(),
      get<SelfServiceCompletedEventManager>(),
      get<CrossSellAfterFlowRepository>(),
    )
  }

  viewModel<EditCoInsuredViewModel> { (contractId: String) ->
    EditCoInsuredViewModel(
      contractId,
      get<GetCoInsuredUseCase>(),
      get<FetchCoInsuredPersonalInformationUseCase>(),
      get<CreateMidtermChangeUseCase>(),
      get<CommitMidtermChangeUseCase>(),
    )
  }

  viewModel<EditCoInsuredTriageViewModel> { (contractId: String?) ->
    EditCoInsuredTriageViewModel(
      get<GetInsurancesForEditCoInsuredUseCase>(),
      contractId,
    )
  }
}
