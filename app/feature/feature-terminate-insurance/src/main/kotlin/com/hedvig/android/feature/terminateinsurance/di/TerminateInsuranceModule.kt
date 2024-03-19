package com.hedvig.android.feature.terminateinsurance.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.data.GetContractsToTerminateUseCase
import com.hedvig.android.feature.terminateinsurance.data.GetContractsToTerminateUseCaseImpl
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminationFlowContextStorage
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationReviewViewModelParameters
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateViewModel
import com.hedvig.android.feature.terminateinsurance.step.start.TerminationStartStepViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.TerminationReviewViewModel
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.language.LanguageService
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val terminateInsuranceModule = module {
  viewModel<ChooseInsuranceToTerminateViewModel> { (insuranceId: String?) ->
    ChooseInsuranceToTerminateViewModel(
      insuranceId = insuranceId,
      getContractsToTerminateUseCase = get<GetContractsToTerminateUseCase>(),
      featureManager = get<FeatureManager>(),
      terminateInsuranceRepository = get<TerminateInsuranceRepository>(),
    )
  }
  viewModel<TerminationStartStepViewModel> { (insuranceId: InsuranceId) ->
    TerminationStartStepViewModel(
      insuranceId = insuranceId,
      terminateInsuranceRepository = get<TerminateInsuranceRepository>(),
    )
  }
  viewModel<TerminationDateViewModel> { (minDate: LocalDate, maxDate: LocalDate) ->
    TerminationDateViewModel(
      minDate = minDate,
      maxDate = maxDate,
      languageService = get<LanguageService>(),
    )
  }
  viewModel<TerminationReviewViewModel> { params ->
    val parameters = params.get<TerminationReviewViewModelParameters>()

    TerminationReviewViewModel(
      terminationType = parameters.terminationType,
      terminateInsuranceRepository = get(),
      clock = get<Clock>(),
      contractGroup = parameters.contractGroup,
      exposureName = parameters.exposureName,
      insuranceDisplayName = parameters.insuranceDisplayName,
    )
  }
  single<TerminateInsuranceRepository> {
    TerminateInsuranceRepository(
      apolloClient = get<ApolloClient>(),
      terminationFlowContextStorage = get(),
    )
  }
  single<GetContractsToTerminateUseCase> {
    GetContractsToTerminateUseCaseImpl(
      apolloClient = get<ApolloClient>(),
    )
  }
  single<TerminationFlowContextStorage> {
    TerminationFlowContextStorage(datastore = get<DataStore<Preferences>>())
  }
}
