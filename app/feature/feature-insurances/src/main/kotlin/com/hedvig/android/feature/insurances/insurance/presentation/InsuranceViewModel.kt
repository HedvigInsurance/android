package com.hedvig.android.feature.insurances.insurance.presentation

import androidx.lifecycle.ViewModel
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.addons.data.GetAddonBannerInfoUseCase
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@Inject
@ViewModelKey
@ContributesIntoMap(ActivityRetainedScope::class, binding<ViewModel>())
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
