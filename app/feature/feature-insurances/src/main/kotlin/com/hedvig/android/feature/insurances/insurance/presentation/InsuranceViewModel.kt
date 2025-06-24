package com.hedvig.android.feature.insurances.insurance.presentation

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCase
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class InsuranceViewModel(
  getInsuranceContractsUseCaseProvider: Provider<GetInsuranceContractsUseCase>,
  getCrossSellsUseCaseProvider: Provider<GetCrossSellsUseCase>,
  getTravelAddonBannerInfoUseCase: Provider<GetTravelAddonBannerInfoUseCase>,
) : MoleculeViewModel<InsuranceScreenEvent, InsuranceUiState>(
    initialState = InsuranceUiState.initialState,
    presenter = InsurancePresenter(
      getInsuranceContractsUseCaseProvider = getInsuranceContractsUseCaseProvider,
      getCrossSellsUseCaseProvider = getCrossSellsUseCaseProvider,
      getTravelAddonBannerInfoUseCase = getTravelAddonBannerInfoUseCase,
    ),
  )
