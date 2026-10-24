package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import octopus.CrossSellsQuery

@Inject
@SingleIn(AppScope::class)
internal class GetCrossSellsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCrossSellsUseCase {
  override fun invoke(): Flow<Either<ErrorMessage, List<CrossSell>>> {
    return apolloClient
      .query(CrossSellsQuery())
      .fetchPolicy(FetchPolicy.CacheAndNetwork)
      .safeFlow(::ErrorMessage)
      .map { crossSellsResponse ->
        either {
          val result = crossSellsResponse.bind()
          val crossSells = result.currentMember.crossSellV2.otherCrossSells.map { crossSell ->
            CrossSell(
              id = crossSell.id,
              title = crossSell.title,
              subtitle = crossSell.description,
              storeUrl = crossSell.storeUrl,
              buttonText = crossSell.buttonTitle,
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
}
