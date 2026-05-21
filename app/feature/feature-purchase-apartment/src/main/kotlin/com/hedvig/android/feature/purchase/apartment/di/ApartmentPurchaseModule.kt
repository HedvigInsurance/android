package com.hedvig.android.feature.purchase.apartment.di

import com.hedvig.android.feature.purchase.apartment.data.CreateSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.apartment.data.CreateSessionAndPriceIntentUseCaseImpl
import com.hedvig.android.feature.purchase.apartment.data.SubmitFormAndGetOffersUseCase
import com.hedvig.android.feature.purchase.apartment.data.SubmitFormAndGetOffersUseCaseImpl
import com.hedvig.android.feature.purchase.apartment.ui.form.ApartmentFormViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val apartmentPurchaseModule = module {
  single<CreateSessionAndPriceIntentUseCase> { CreateSessionAndPriceIntentUseCaseImpl(apolloClient = get()) }
  single<SubmitFormAndGetOffersUseCase> { SubmitFormAndGetOffersUseCaseImpl(apolloClient = get()) }

  viewModel<ApartmentFormViewModel> { params ->
    ApartmentFormViewModel(
      productName = params.get(),
      createSessionAndPriceIntentUseCase = get(),
      submitFormAndGetOffersUseCase = get(),
    )
  }
}
