package com.hedvig.android.feature.insurances.insurance.presentation

import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.addons.data.GetAddonBannerInfoUseCase
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class InsuranceViewModel(
  getInsuranceContractsUseCase: GetInsuranceContractsUseCase,
  getCrossSellsUseCase: GetCrossSellsUseCase,
  getAddonBannerInfoUseCase: GetAddonBannerInfoUseCase,
) : MoleculeViewModel<InsuranceScreenEvent, InsuranceUiState>(
    initialState = InsuranceUiState.initialState,
    presenter = InsurancePresenter(
      getInsuranceContractsUseCase = getInsuranceContractsUseCase,
      getCrossSellsUseCase = getCrossSellsUseCase,
      getAddonBannerInfoUseCase = getAddonBannerInfoUseCase,
    ),
  )
