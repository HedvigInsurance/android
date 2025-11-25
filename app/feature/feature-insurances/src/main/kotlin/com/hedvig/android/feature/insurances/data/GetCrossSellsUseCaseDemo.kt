package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset

internal class GetCrossSellsUseCaseDemo : GetCrossSellsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, CrossSellResult> {
    return either {
      CrossSellResult(
        false,
        listOf(
          CrossSell(
            "1",
            "Home Insurance",
            "For you, your family and your home",
            "",
            ImageAsset("", "", ""),
          ),
          CrossSell(
            "2",
            "Pet Insurance",
            "For your dog or cat",
            "",
            ImageAsset("", "", ""),
          ),
          CrossSell(
            "3",
            "Car Insurance",
            "For your and your car",
            "",
            ImageAsset("", "", ""),
          ),
          CrossSell(
            "4",
            "Accident Insurance",
            "No loopholes on our part. No worries on your part.",
            "",
            ImageAsset("", "", ""),
          ),
        )
      )

    }
  }
}
