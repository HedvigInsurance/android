package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import octopus.CrossSellsQuery

internal class GetCrossSellsUseCaseDemo : GetCrossSellsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<CrossSellsQuery.Data.CurrentMember.CrossSell>> {
    return either {
      listOf(
        CrossSellsQuery.Data.CurrentMember.CrossSell(
          "1",
          "Home Insurance",
          "For you, your family and your home",
          "",
          octopus.CrossSellsQuery.Data.CurrentMember.CrossSell.PillowImageLarge("", "", ""),
        ),
        CrossSellsQuery.Data.CurrentMember.CrossSell(
          "2",
          "Pet Insurance",
          "For your dog or cat",
          "",
          octopus.CrossSellsQuery.Data.CurrentMember.CrossSell.PillowImageLarge("", "", ""),
        ),
        CrossSellsQuery.Data.CurrentMember.CrossSell(
          "3",
          "Car Insurance",
          "For your and your car",
          "",
          octopus.CrossSellsQuery.Data.CurrentMember.CrossSell.PillowImageLarge("", "", ""),
        ),
        CrossSellsQuery.Data.CurrentMember.CrossSell(
          "4",
          "Accident Insurance",
          "No loopholes on our part. No worries on your part.",
          "",
          octopus.CrossSellsQuery.Data.CurrentMember.CrossSell.PillowImageLarge("", "", ""),
        ),
      )
    }
  }
}
