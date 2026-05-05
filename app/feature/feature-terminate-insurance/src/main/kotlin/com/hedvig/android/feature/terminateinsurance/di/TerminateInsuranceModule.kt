package com.hedvig.android.feature.terminateinsurance.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.termination.data.GetTerminatableContractsUseCase
import com.hedvig.android.feature.terminateinsurance.data.ExtraCoverageItem
import com.hedvig.android.feature.terminateinsurance.data.GetTerminationNotificationUseCase
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepositoryImpl
import com.hedvig.android.feature.terminateinsurance.data.TerminationAction
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationDateParameters
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateViewModel
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.TerminationConfirmationViewModel
import com.hedvig.android.language.LanguageService
import kotlin.time.Clock
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val terminateInsuranceModule = module {
  viewModel<ChooseInsuranceToTerminateViewModel> { (insuranceId: String?) ->
    ChooseInsuranceToTerminateViewModel(
      insuranceId = insuranceId,
      getTerminatableContractsUseCase = get<GetTerminatableContractsUseCase>(),
      terminateInsuranceRepository = get<TerminateInsuranceRepository>(),
    )
  }
  viewModel<TerminationSurveyViewModel> {
    (
      options: List<TerminationSurveyOption>,
      action: TerminationAction,
      contractId: String,
    ),
    ->
    TerminationSurveyViewModel(
      options = options,
      action = action,
      changeTierRepository = get<ChangeTierRepository>(),
      contractId = contractId,
    )
  }
  viewModel<TerminationDateViewModel> { (parameters: TerminationDateParameters) ->
    TerminationDateViewModel(
      parameters,
      languageService = get<LanguageService>(),
    )
  }
  viewModel<TerminationConfirmationViewModel> {
    (
      terminationType: TerminateInsuranceDestination.TerminationConfirmation.TerminationType,
      insuranceInfo: TerminationGraphParameters,
      extraCoverageItems: List<ExtraCoverageItem>,
      selectedReasonId: String,
      feedbackComment: String?,
    ),
    ->
    TerminationConfirmationViewModel(
      terminationType = terminationType,
      insuranceInfo = insuranceInfo,
      extraCoverageItems = extraCoverageItems,
      selectedReasonId = selectedReasonId,
      feedbackComment = feedbackComment,
      terminateInsuranceRepository = get<TerminateInsuranceRepository>(),
      getTerminationNotificationUseCase = get<GetTerminationNotificationUseCase>(),
      clock = get<Clock>(),
    )
  }

  single<TerminateInsuranceRepository> {
    TerminateInsuranceRepositoryImpl(
      apolloClient = get<ApolloClient>(),
    )
  }
  single<GetTerminationNotificationUseCase> {
    GetTerminationNotificationUseCase(get<ApolloClient>())
  }
}
