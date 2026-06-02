package com.hedvig.android.feature.payments.ui.payments

import androidx.lifecycle.ViewModel
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.payments.overview.data.GetShouldShowPayoutUseCase
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
internal class PaymentsViewModel(
  getUpcomingPaymentUseCase: Provider<GetUpcomingPaymentUseCase>,
  getShouldShowPayoutUseCase: Provider<GetShouldShowPayoutUseCase>,
) : MoleculeViewModel<PaymentsEvent, PaymentsUiState>(
    PaymentsUiState.Loading,
    PaymentsPresenter(
      getUpcomingPaymentUseCase = getUpcomingPaymentUseCase,
      getShouldShowPayoutUseCase = getShouldShowPayoutUseCase,
    ),
  )
