package com.hedvig.android.feature.terminateinsurance.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminationFlowContextStorage
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.step.deletion.InsuranceDeletionViewModel
import com.hedvig.android.feature.terminateinsurance.step.overview.OverviewViewModel
import com.hedvig.android.feature.terminateinsurance.step.start.TerminationStartStepViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateViewModel
import com.hedvig.android.navigation.core.AppDestination
import kotlinx.datetime.LocalDate
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val terminateInsuranceModule = module {
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
    )
  }
  viewModel<InsuranceDeletionViewModel> { (insuranceDeletion: TerminateInsuranceDestination.InsuranceDeletion) ->
    InsuranceDeletionViewModel(
      insuranceDeletion = insuranceDeletion,
      terminateInsuranceRepository = get<TerminateInsuranceRepository>(),
    )
  }
  viewModel<OverviewViewModel> { (selectedDate: LocalDate, destination: AppDestination.TerminateInsurance) ->
    OverviewViewModel(
      selectedDate = selectedDate,
      destination = destination,
      terminateInsuranceRepository = get(),
    )
  }
  single<TerminateInsuranceRepository> {
    TerminateInsuranceRepository(
      apolloClient = get<ApolloClient>(),
      terminationFlowContextStorage = get(),
    )
  }
  single<TerminationFlowContextStorage> {
    TerminationFlowContextStorage(datastore = get<DataStore<Preferences>>())
  }
}
