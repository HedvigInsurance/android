package com.hedvig.android.feature.payments.ui.discounts

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.feature.payments.data.GetDiscountsOverviewUseCase
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class DiscountsViewModel(
  getDiscountsOverviewUseCase: GetDiscountsOverviewUseCase,
) : MoleculeViewModel<DiscountsEvent, DiscountsUiState>(
    DiscountsUiState(foreverInformation = null, discountedContracts = listOf()),
    DiscountsPresenter(
      getDiscountsOverviewUseCase = getDiscountsOverviewUseCase,
    ),
  )
