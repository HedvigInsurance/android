package com.hedvig.android.feature.terminateinsurance.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.data.termination.data.GetTerminatableContractsUseCase
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepositoryImpl
import com.hedvig.android.feature.terminateinsurance.data.TerminationFlowContextStorage
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationDateParameters
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateViewModel
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.TerminationConfirmationViewModel
import com.hedvig.android.language.LanguageService
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val terminateInsuranceModule = module {
  viewModel<ChooseInsuranceToTerminateViewModel> { (insuranceId: String?) ->
    ChooseInsuranceToTerminateViewModel(
      insuranceId = insuranceId,
      getTerminatableContractsUseCase = get<GetTerminatableContractsUseCase>(),
      terminateInsuranceRepository = get<TerminateInsuranceRepository>(),
    )
  }
  viewModel<TerminationSurveyViewModel> { (options: List<TerminationSurveyOption>) ->
    TerminationSurveyViewModel(
      options = options,
      terminateInsuranceRepository = get<TerminateInsuranceRepository>(),
    )
  }
  viewModel<TerminationDateViewModel> { (parameters: TerminationDateParameters) ->
    TerminationDateViewModel(
      parameters,
      languageService = get<LanguageService>(),
    )
  }
  viewModel<TerminationConfirmationViewModel> { params ->
    val terminationType = params.get<TerminateInsuranceDestination.TerminationConfirmation.TerminationType>()

    TerminationConfirmationViewModel(
      terminationType = terminationType,
      terminateInsuranceRepository = get(),
    )
  }
  single<TerminateInsuranceRepository> {
    TerminateInsuranceRepositoryImpl(
      apolloClient = get<ApolloClient>(),
      terminationFlowContextStorage = get(),
    )
  }
  single<TerminationFlowContextStorage> {
    TerminationFlowContextStorage(datastore = get<DataStore<Preferences>>())
  }
}
