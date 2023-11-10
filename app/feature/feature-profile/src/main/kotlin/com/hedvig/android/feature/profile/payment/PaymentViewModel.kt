package com.hedvig.android.feature.profile.payment

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.forever.ForeverRepository
import com.hedvig.android.data.payment.PaymentRepository
import com.hedvig.android.language.LanguageService
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class PaymentViewModel(
  private val referralsRepositoryProvider: Provider<ForeverRepository>,
  private val paymentRepositoryProvider: Provider<PaymentRepository>,
  val languageService: LanguageService,
) : MoleculeViewModel<PaymentEvent, PaymentUiState>(
    PaymentUiState(isLoading = true),
    PaymentPresenter(referralsRepositoryProvider, paymentRepositoryProvider, languageService),
  )
