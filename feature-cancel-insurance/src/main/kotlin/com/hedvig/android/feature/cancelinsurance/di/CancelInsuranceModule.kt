package com.hedvig.android.feature.cancelinsurance.di

import com.hedvig.android.feature.cancelinsurance.CancelInsuranceViewModel
import com.hedvig.android.feature.cancelinsurance.InsuranceId
import com.hedvig.android.feature.cancelinsurance.data.CancelInsuranceUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val cancelInsuranceModule = module {
  viewModel<CancelInsuranceViewModel> { (insuranceId: InsuranceId) ->
    CancelInsuranceViewModel(insuranceId, get())
  }
  single<CancelInsuranceUseCase> { CancelInsuranceUseCase(get()) }
}
