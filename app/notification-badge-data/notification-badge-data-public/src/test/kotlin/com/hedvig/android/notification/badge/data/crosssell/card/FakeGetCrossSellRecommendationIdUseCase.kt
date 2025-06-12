package com.hedvig.android.notification.badge.data.crosssell.card

import com.hedvig.android.notification.badge.data.crosssell.CrossSellIdentifier
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellRecommendationIdUseCase

class FakeGetCrossSellRecommendationIdUseCase(
  private val typeOfContracts: (() -> Set<CrossSellIdentifier>) = { emptySet() },
) : GetCrossSellRecommendationIdUseCase {
  override suspend fun invoke(): Set<CrossSellIdentifier> {
    return typeOfContracts()
  }
}
