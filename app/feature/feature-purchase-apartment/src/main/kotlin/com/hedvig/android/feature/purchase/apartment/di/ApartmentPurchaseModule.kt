package com.hedvig.android.feature.purchase.apartment.di

import com.hedvig.android.feature.purchase.apartment.data.AddToCartAndStartSignUseCase
import com.hedvig.android.feature.purchase.apartment.data.AddToCartAndStartSignUseCaseImpl
import com.hedvig.android.feature.purchase.apartment.data.CreateSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.apartment.data.CreateSessionAndPriceIntentUseCaseImpl
import com.hedvig.android.feature.purchase.apartment.data.PollSigningStatusUseCase
import com.hedvig.android.feature.purchase.apartment.data.PollSigningStatusUseCaseImpl
import com.hedvig.android.feature.purchase.apartment.data.SubmitFormAndGetOffersUseCase
import com.hedvig.android.feature.purchase.apartment.data.SubmitFormAndGetOffersUseCaseImpl
import com.hedvig.android.feature.purchase.apartment.ui.form.ApartmentFormViewModel
import com.hedvig.android.feature.purchase.apartment.ui.offer.SelectTierViewModel
import com.hedvig.android.feature.purchase.apartment.ui.sign.SigningViewModel
import com.hedvig.android.feature.purchase.apartment.ui.summary.PurchaseSummaryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val apartmentPurchaseModule = module {
  single<CreateSessionAndPriceIntentUseCase> { CreateSessionAndPriceIntentUseCaseImpl(apolloClient = get()) }
  single<SubmitFormAndGetOffersUseCase> { SubmitFormAndGetOffersUseCaseImpl(apolloClient = get()) }
  single<AddToCartAndStartSignUseCase> { AddToCartAndStartSignUseCaseImpl(apolloClient = get()) }
  single<PollSigningStatusUseCase> { PollSigningStatusUseCaseImpl(apolloClient = get()) }

  viewModel<ApartmentFormViewModel> { params ->
    ApartmentFormViewModel(
      productName = params.get(),
      createSessionAndPriceIntentUseCase = get(),
      submitFormAndGetOffersUseCase = get(),
    )
  }
  viewModel<SelectTierViewModel> { params ->
    SelectTierViewModel(params = params.get())
  }
  viewModel<PurchaseSummaryViewModel> { params ->
    PurchaseSummaryViewModel(
      summaryParameters = params.get(),
      addToCartAndStartSignUseCase = get(),
    )
  }
  viewModel<SigningViewModel> { params ->
    SigningViewModel(
      signingParameters = params.get(),
      pollSigningStatusUseCase = get(),
    )
  }
}
