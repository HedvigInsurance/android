package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import octopus.CrossSalesQuery
import octopus.type.CrossSellType

internal class GetCrossSellsUseCaseDemo : GetCrossSellsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<CrossSalesQuery.Data.CurrentMember.CrossSell>> {
    return either {
      listOf(
        CrossSalesQuery.Data.CurrentMember.CrossSell(
          "1",
          "Home Insurance",
          "For you, your family and your home",
          "",
          CrossSellType.HOME,
        ),
        CrossSalesQuery.Data.CurrentMember.CrossSell(
          "2",
          "Pet Insurance",
          "For your dog or cat",
          "",
          CrossSellType.PET,
        ),
        CrossSalesQuery.Data.CurrentMember.CrossSell(
          "3",
          "Car Insurance",
          "For your and your car",
          "",
          CrossSellType.CAR,
        ),
        CrossSalesQuery.Data.CurrentMember.CrossSell(
          "4",
          "Accident Insurance",
          "No loopholes on our part. No worries on your part.",
          "",
          CrossSellType.ACCIDENT,
        ),
      )
    }
  }
}
