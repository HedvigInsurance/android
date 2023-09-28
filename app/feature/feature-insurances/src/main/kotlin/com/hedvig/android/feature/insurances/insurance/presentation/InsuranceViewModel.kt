package com.hedvig.android.feature.insurances.insurance.presentation

import com.hedvig.android.feature.insurances.di.GetCrossSellsUseCaseProvider
import com.hedvig.android.feature.insurances.di.GetInsuranceContractsUseCaseProvider
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService

internal class InsuranceViewModel(
  getInsuranceContractsUseCaseProvider: GetInsuranceContractsUseCaseProvider,
  getCrossSellsUseCaseProvider: GetCrossSellsUseCaseProvider,
  crossSellCardNotificationBadgeService: CrossSellCardNotificationBadgeService,
) : MoleculeViewModel<InsuranceScreenEvent, InsuranceUiState>(
  initialState = InsuranceUiState.initialState,
  presenter = InsurancePresenter(
    getInsuranceContractsUseCaseProvider = getInsuranceContractsUseCaseProvider,
    getCrossSellsUseCaseProvider = getCrossSellsUseCaseProvider,
    crossSellCardNotificationBadgeService = crossSellCardNotificationBadgeService,
  ),
)
