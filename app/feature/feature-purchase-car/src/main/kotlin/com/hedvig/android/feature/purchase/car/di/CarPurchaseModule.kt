package com.hedvig.android.feature.purchase.car.di

import com.hedvig.android.feature.purchase.car.data.CreateCarSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.car.data.CreateCarSessionAndPriceIntentUseCaseImpl
import com.hedvig.android.feature.purchase.car.data.SubmitCarFormAndGetOffersUseCase
import com.hedvig.android.feature.purchase.car.data.SubmitCarFormAndGetOffersUseCaseImpl
import com.hedvig.android.feature.purchase.car.ui.form.CarFormViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val carPurchaseModule = module {
  single<CreateCarSessionAndPriceIntentUseCase> { CreateCarSessionAndPriceIntentUseCaseImpl(apolloClient = get()) }
  single<SubmitCarFormAndGetOffersUseCase> { SubmitCarFormAndGetOffersUseCaseImpl(apolloClient = get()) }

  viewModel<CarFormViewModel> { params ->
    CarFormViewModel(
      productName = params.get(),
      createCarSessionAndPriceIntentUseCase = get(),
      submitCarFormAndGetOffersUseCase = get(),
    )
  }
}
