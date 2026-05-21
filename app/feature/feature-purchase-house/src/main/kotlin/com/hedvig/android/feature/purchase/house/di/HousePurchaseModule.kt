package com.hedvig.android.feature.purchase.house.di

import com.hedvig.android.feature.purchase.house.data.CreateHouseSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.house.data.CreateHouseSessionAndPriceIntentUseCaseImpl
import com.hedvig.android.feature.purchase.house.data.SubmitHouseFormAndGetOffersUseCase
import com.hedvig.android.feature.purchase.house.data.SubmitHouseFormAndGetOffersUseCaseImpl
import com.hedvig.android.feature.purchase.house.data.SubmitVacationHomeFormAndGetOffersUseCase
import com.hedvig.android.feature.purchase.house.data.SubmitVacationHomeFormAndGetOffersUseCaseImpl
import com.hedvig.android.feature.purchase.house.ui.house.HouseFormViewModel
import com.hedvig.android.feature.purchase.house.ui.vacationhome.VacationHomeFormViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val housePurchaseModule = module {
  single<CreateHouseSessionAndPriceIntentUseCase> {
    CreateHouseSessionAndPriceIntentUseCaseImpl(apolloClient = get())
  }
  single<SubmitVacationHomeFormAndGetOffersUseCase> {
    SubmitVacationHomeFormAndGetOffersUseCaseImpl(apolloClient = get())
  }
  single<SubmitHouseFormAndGetOffersUseCase> {
    SubmitHouseFormAndGetOffersUseCaseImpl(apolloClient = get())
  }

  viewModel<VacationHomeFormViewModel> { params ->
    VacationHomeFormViewModel(
      productName = params.get(),
      createHouseSessionAndPriceIntentUseCase = get(),
      submitVacationHomeFormAndGetOffersUseCase = get(),
    )
  }

  viewModel<HouseFormViewModel> { params ->
    HouseFormViewModel(
      productName = params.get(),
      createHouseSessionAndPriceIntentUseCase = get(),
      submitHouseFormAndGetOffersUseCase = get(),
    )
  }
}
