package com.hedvig.android.feature.terminateinsurance.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.termination.data.GetTerminatableContractsUseCase
import com.hedvig.android.feature.terminateinsurance.data.ExtraCoverageItem
import com.hedvig.android.feature.terminateinsurance.data.GetTerminationNotificationUseCase
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepositoryImpl
import com.hedvig.android.feature.terminateinsurance.data.TerminationFlowContextStorage
import com.hedvig.android.feature.terminateinsurance.data.TerminationInfo
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.feature.terminateinsurance.navigation.AutoDecommissionDeflectStepParameters
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationDateParameters
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateViewModel
import com.hedvig.android.feature.terminateinsurance.step.deflectAutoDecom.DeflectAutoDecommissionStepViewModel
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.TerminationConfirmationViewModel
import com.hedvig.android.featureflags.FeatureManager
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
  viewModel<TerminationSurveyViewModel> { params ->
    val options = params.get<List<TerminationSurveyOption>>()
    val terminationInfo = params.getOrNull<TerminationInfo>()
    TerminationSurveyViewModel(
      options = options,
      terminateInsuranceRepository = get<TerminateInsuranceRepository>(),
      changeTierRepository = get<ChangeTierRepository>(),
      terminationInfo = terminationInfo,
    )
  }
  viewModel<TerminationDateViewModel> { (parameters: TerminationDateParameters) ->
    TerminationDateViewModel(
      parameters,
      languageService = get<LanguageService>(),
    )
  }
  viewModel<TerminationConfirmationViewModel> { params ->
    TerminationConfirmationViewModel(
      terminationType = params.component1(),
      insuranceInfo = params.component2(),
      extraCoverageItems = params.component3(),
      terminateInsuranceRepository = get<TerminateInsuranceRepository>(),
      getTerminationNotificationUseCase = get<GetTerminationNotificationUseCase>(),
      clock = get<Clock>(),
      selectedOptionId = params.component4(),
      feedbackText = params.component5(),
    )
  }

  viewModel<DeflectAutoDecommissionStepViewModel> { params ->
    val deflectParams = params.get<AutoDecommissionDeflectStepParameters>()
    val terminationInfo = params.getOrNull<TerminationInfo>()
    DeflectAutoDecommissionStepViewModel(
      terminateInsuranceRepository = get<TerminateInsuranceRepository>(),
      deflectParameters = deflectParams,
      terminationInfo = terminationInfo,
    )
  }

  single<TerminateInsuranceRepository> {
    TerminateInsuranceRepositoryImpl(
      apolloClient = get<ApolloClient>(),
      featureManager = get<FeatureManager>(),
      terminationFlowContextStorage = get<TerminationFlowContextStorage>(),
    )
  }
  single<GetTerminationNotificationUseCase> {
    GetTerminationNotificationUseCase(get<ApolloClient>())
  }
  single<TerminationFlowContextStorage> {
    TerminationFlowContextStorage(datastore = get<DataStore<Preferences>>())
  }
}
