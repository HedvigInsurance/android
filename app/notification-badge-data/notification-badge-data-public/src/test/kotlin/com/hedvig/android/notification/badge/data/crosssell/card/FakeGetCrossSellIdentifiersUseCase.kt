package com.hedvig.android.notification.badge.data.crosssell.card

import com.hedvig.android.notification.badge.data.crosssell.CrossSellIdentifier
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellIdentifiersUseCase

class FakeGetCrossSellIdentifiersUseCase(
  private val typeOfContracts: (() -> Set<CrossSellIdentifier>) = { emptySet() },
) : GetCrossSellIdentifiersUseCase {
  override suspend fun invoke(): Set<CrossSellIdentifier> {
    return typeOfContracts()
  }
}
