package com.hedvig.android.feature.insurances.insurance.presentation

import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.addons.data.GetAddonBannerInfoUseCase
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel
internal class InsuranceViewModel(
  getInsuranceContractsUseCaseProvider: Provider<GetInsuranceContractsUseCase>,
  getCrossSellsUseCaseProvider: Provider<GetCrossSellsUseCase>,
  getAddonBannerInfoUseCase: Provider<GetAddonBannerInfoUseCase>,
) : MoleculeViewModel<InsuranceScreenEvent, InsuranceUiState>(
    initialState = InsuranceUiState.initialState,
    presenter = InsurancePresenter(
      getInsuranceContractsUseCaseProvider = getInsuranceContractsUseCaseProvider,
      getCrossSellsUseCaseProvider = getCrossSellsUseCaseProvider,
      getAddonBannerInfoUseCase = getAddonBannerInfoUseCase,
    ),
  )
