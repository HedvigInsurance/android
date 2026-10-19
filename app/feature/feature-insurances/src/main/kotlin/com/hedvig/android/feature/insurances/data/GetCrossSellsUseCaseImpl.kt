package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.data.contract.InAppProduct
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.Serializable
import octopus.CrossSellsQuery

@Inject
@SingleIn(AppScope::class)
internal class GetCrossSellsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCrossSellsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<CrossSell>> {
    return either {
      val result = apolloClient
        .query(CrossSellsQuery())
        .fetchPolicy(FetchPolicy.NetworkFirst)
        .safeExecute(::ErrorMessage)
        .bind()
      val crossSells = result.currentMember.crossSellV2.otherCrossSells.map { crossSell ->
        CrossSell(
          id = crossSell.id,
          title = crossSell.title,
          subtitle = crossSell.description,
          storeUrl = crossSell.storeUrl,
          buttonText = crossSell.buttonTitle,
          inAppProduct = crossSell.inAppProduct?.toInAppProduct(),
          pillowImageSmall = ImageAsset(
            id = crossSell.pillowImageSmall.id,
            src = crossSell.pillowImageSmall.src,
            description = crossSell.pillowImageSmall.alt,
          ),
          pillowImageLarge = ImageAsset(
            id = crossSell.pillowImageLarge.id,
            src = crossSell.pillowImageLarge.src,
            description = crossSell.pillowImageLarge.alt,
          ),
        )
      }
      crossSells
    }
  }
}

private fun octopus.type.InAppProduct.toInAppProduct(): InAppProduct? = when (this) {
  octopus.type.InAppProduct.ACCIDENT -> InAppProduct.ACCIDENT
  octopus.type.InAppProduct.PAYMENT_PROTECTION -> InAppProduct.PAYMENT_PROTECTION
  octopus.type.InAppProduct.UNKNOWN__ -> null
}
