package com.hedvig.android.feature.insurances.insurance.presentation

import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService

internal class InsuranceViewModel(
  getInsuranceContractsUseCase: GetInsuranceContractsUseCase,
  getCrossSellsUseCase: GetCrossSellsUseCase,
  crossSellCardNotificationBadgeService: CrossSellCardNotificationBadgeService,
) : MoleculeViewModel<InsuranceScreenEvent, InsuranceUiState>(
  initialState = InsuranceUiState.initialState,
  presenter = InsurancePresenter(
    getInsuranceContractsUseCase = getInsuranceContractsUseCase,
    getCrossSellsUseCase = getCrossSellsUseCase,
    crossSellCardNotificationBadgeService = crossSellCardNotificationBadgeService,
  ),
)
