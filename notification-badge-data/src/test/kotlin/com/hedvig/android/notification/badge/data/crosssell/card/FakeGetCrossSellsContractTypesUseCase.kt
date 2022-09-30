package com.hedvig.android.notification.badge.data.crosssell.card

import com.hedvig.android.apollo.graphql.type.TypeOfContract
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellsContractTypesUseCase

class FakeGetCrossSellsContractTypesUseCase(
  private val typeOfContracts: (() -> Set<TypeOfContract>) = { emptySet() },
) : GetCrossSellsContractTypesUseCase {
  override suspend fun invoke(): Set<TypeOfContract> {
    return typeOfContracts()
  }
}
