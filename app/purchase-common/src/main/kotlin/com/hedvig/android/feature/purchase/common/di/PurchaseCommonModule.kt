package com.hedvig.android.feature.purchase.common.di

import com.hedvig.android.feature.purchase.common.data.AddToCartAndStartSignUseCase
import com.hedvig.android.feature.purchase.common.data.AddToCartAndStartSignUseCaseImpl
import com.hedvig.android.feature.purchase.common.data.PollSigningStatusUseCase
import com.hedvig.android.feature.purchase.common.data.PollSigningStatusUseCaseImpl
import com.hedvig.android.feature.purchase.common.ui.offer.SelectTierViewModel
import com.hedvig.android.feature.purchase.common.ui.sign.SigningViewModel
import com.hedvig.android.feature.purchase.common.ui.summary.PurchaseSummaryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val purchaseCommonModule = module {
  single<AddToCartAndStartSignUseCase> { AddToCartAndStartSignUseCaseImpl(apolloClient = get()) }
  single<PollSigningStatusUseCase> { PollSigningStatusUseCaseImpl(apolloClient = get()) }

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
