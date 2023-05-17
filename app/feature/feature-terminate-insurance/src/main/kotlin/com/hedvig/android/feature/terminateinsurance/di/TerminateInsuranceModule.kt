package com.hedvig.android.feature.terminateinsurance.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.step.deletion.InsuranceDeletionViewModel
import com.hedvig.android.feature.terminateinsurance.step.start.TerminationStartStepViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateViewModel
import kotlinx.datetime.LocalDate
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val terminateInsuranceModule = module {
  viewModel<TerminationStartStepViewModel> { (insuranceId: InsuranceId) ->
    TerminationStartStepViewModel(insuranceId, get<TerminateInsuranceRepository>())
  }
  viewModel<TerminationDateViewModel> { (minDate: LocalDate, maxDate: LocalDate) ->
    TerminationDateViewModel(minDate, maxDate, get<TerminateInsuranceRepository>())
  }
  viewModel<InsuranceDeletionViewModel> { (insuranceDeletion: TerminateInsuranceDestination.InsuranceDeletion) ->
    InsuranceDeletionViewModel(insuranceDeletion, get<TerminateInsuranceRepository>())
  }
  single<TerminateInsuranceRepository> { TerminateInsuranceRepository(get<ApolloClient>(octopusClient)) }
}
