package com.hedvig.android.feature.payments.payments

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.market.MarketManager
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class PaymentsViewModel(
  getUpcomingPaymentUseCase: Provider<GetUpcomingPaymentUseCase>,
  marketManager: MarketManager,
) : MoleculeViewModel<PaymentsEvent, PaymentsUiState>(
    PaymentsUiState.Loading,
    PaymentsPresenter(
      getUpcomingPaymentUseCase = getUpcomingPaymentUseCase,
      marketManager = marketManager,
    ),
  )
