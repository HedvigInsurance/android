package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import kotlinx.serialization.Serializable
import octopus.CrossSellsQuery

internal class GetCrossSellsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCrossSellsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, CrossSellResult> {
    return either {
      val result = apolloClient
        .query(CrossSellsQuery())
        .safeExecute(::ErrorMessage)
        .bind()
      val crossSells = result.currentMember.crossSellV2.otherCrossSells.map { crossSell ->
        CrossSell(
          id = crossSell.id,
          title = crossSell.title,
          subtitle = crossSell.description,
          storeUrl = crossSell.storeUrl,
          buttonText = crossSell.buttonTitle,
          pillowImage = ImageAsset(
            id = crossSell.pillowImageLarge.id,
            src = crossSell.pillowImageLarge.src,
            description = crossSell.pillowImageLarge.alt,
          ),
        )
      }
      CrossSellResult(result.currentMember.crossSellV2.discountAvailable, crossSells)
    }
  }
}

data class CrossSellResult(
  val hasDiscounts: Boolean,
  val crossSells: List<CrossSell>
)
