package com.hedvig.android.notification.badge.data.crosssell.card

import com.hedvig.android.notification.badge.data.crosssell.CrossSellIdentifier
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellRecommendationIdUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeGetCrossSellRecommendationIdUseCase(
  private val recommendationId: (() -> CrossSellIdentifier?) = { null },
) : GetCrossSellRecommendationIdUseCase {
  override fun invoke(): Flow<CrossSellIdentifier?> {
    return flowOf(recommendationId())
  }
}
