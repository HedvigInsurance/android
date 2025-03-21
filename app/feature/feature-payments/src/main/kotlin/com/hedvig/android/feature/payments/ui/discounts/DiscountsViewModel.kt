package com.hedvig.android.feature.payments.ui.discounts

import com.hedvig.android.feature.payments.data.GetDiscountsOverviewUseCase
import com.hedvig.android.feature.payments.overview.data.AddDiscountUseCase
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class DiscountsViewModel(
  getDiscountsOverviewUseCase: GetDiscountsOverviewUseCase,
  addDiscountUseCase: AddDiscountUseCase,
  featureManager: FeatureManager,
) : MoleculeViewModel<DiscountsEvent, DiscountsUiState>(
    DiscountsUiState(foreverInformation = null, discounts = listOf(), allowAddingCampaignCode = false),
    DiscountsPresenter(
      getDiscountsOverviewUseCase = getDiscountsOverviewUseCase,
      addDiscountUseCase = addDiscountUseCase,
      featureManager = featureManager,
    ),
  )
