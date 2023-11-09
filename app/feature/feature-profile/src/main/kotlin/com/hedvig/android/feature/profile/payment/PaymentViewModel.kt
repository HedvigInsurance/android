package com.hedvig.android.feature.profile.payment

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.forever.ForeverRepository
import com.hedvig.android.language.LanguageService
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.payment.PaymentRepository

internal class PaymentViewModel(
  private val referralsRepositoryProvider: Provider<ForeverRepository>,
  private val paymentRepositoryProvider: Provider<PaymentRepository>,
  val languageService: LanguageService,
) : MoleculeViewModel<PaymentEvent, PaymentUiState>(
    PaymentUiState(isLoading = true),
    PaymentPresenter(referralsRepositoryProvider, paymentRepositoryProvider, languageService),
  )
