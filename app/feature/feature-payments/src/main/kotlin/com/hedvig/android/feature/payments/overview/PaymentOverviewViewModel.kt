package com.hedvig.android.feature.payments.overview

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.payments.overview.data.AddDiscountUseCase
import com.hedvig.android.feature.payments.overview.data.GetPaymentOverviewDataUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class PaymentOverviewViewModel(
  getPaymentOverviewDataUseCase: Provider<GetPaymentOverviewDataUseCase>,
  addDiscountUseCase: AddDiscountUseCase,
) : MoleculeViewModel<PaymentEvent, OverViewUiState>(
    OverViewUiState(foreverInformation = null),
    PaymentOverviewPresenter(
      getPaymentOverviewDataUseCase = getPaymentOverviewDataUseCase,
      addDiscountUseCase = addDiscountUseCase,
    ),
  )
