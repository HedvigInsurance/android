package com.hedvig.android.cancelinsurance.di

import com.hedvig.android.cancelinsurance.CancelInsuranceViewModel
import com.hedvig.android.cancelinsurance.InsuranceId
import com.hedvig.android.cancelinsurance.data.CancelInsuranceUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val cancelInsuranceModule = module {
  viewModel<CancelInsuranceViewModel> { (insuranceId: InsuranceId) ->
    CancelInsuranceViewModel(insuranceId, get())
  }
  single<CancelInsuranceUseCase> { CancelInsuranceUseCase(get()) }
}
