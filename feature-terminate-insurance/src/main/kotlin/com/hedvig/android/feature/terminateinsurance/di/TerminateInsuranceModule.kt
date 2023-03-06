package com.hedvig.android.feature.terminateinsurance.di

import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.TerminateInsuranceViewModel
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val terminateInsuranceModule = module {
  viewModel<TerminateInsuranceViewModel> { (insuranceId: InsuranceId) ->
    TerminateInsuranceViewModel(insuranceId, get())
  }
  single<TerminateInsuranceUseCase> { TerminateInsuranceUseCase(get()) }
}
