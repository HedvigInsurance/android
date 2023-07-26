package com.hedvig.android.notification.badge.data.crosssell.card

import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellsContractTypesUseCase
import giraffe.type.TypeOfContract

class FakeGetCrossSellsContractTypesUseCase(
  private val typeOfContracts: (() -> Set<TypeOfContract>) = { emptySet() },
) : GetCrossSellsContractTypesUseCase {
  override suspend fun invoke(): Set<TypeOfContract> {
    return typeOfContracts()
  }
}
