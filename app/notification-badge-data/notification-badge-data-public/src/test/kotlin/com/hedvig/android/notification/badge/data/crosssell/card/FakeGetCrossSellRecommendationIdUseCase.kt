package com.hedvig.android.notification.badge.data.crosssell.card

import com.hedvig.android.notification.badge.data.crosssell.CrossSellIdentifier
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellRecommendationIdUseCase

class FakeGetCrossSellRecommendationIdUseCase(
  private val recommendationId: (() -> CrossSellIdentifier?) = { null },
) : GetCrossSellRecommendationIdUseCase {
  override suspend fun invoke(): CrossSellIdentifier? {
    return recommendationId()
  }
}
