package com.hedvig.android.feature.insurances

import com.hedvig.android.core.ui.MoleculeViewModel
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.insurance.present.InsurancePresenter
import com.hedvig.android.feature.insurances.insurance.present.InsuranceScreenEvent
import com.hedvig.android.feature.insurances.insurance.present.InsuranceUiState
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService

internal class InsuranceViewModel(
  private val getInsuranceContractsUseCase: GetInsuranceContractsUseCase,
  private val getCrossSellsUseCase: GetCrossSellsUseCase,
  private val crossSellCardNotificationBadgeService: CrossSellCardNotificationBadgeService,
) : MoleculeViewModel<InsuranceScreenEvent, InsuranceUiState>(
  initialState = InsuranceUiState.InitialState,
  presenter = InsurancePresenter(
    getInsuranceContractsUseCase = getInsuranceContractsUseCase,
    getCrossSellsUseCase = getCrossSellsUseCase,
    crossSellCardNotificationBadgeService = crossSellCardNotificationBadgeService,
  ),
)
