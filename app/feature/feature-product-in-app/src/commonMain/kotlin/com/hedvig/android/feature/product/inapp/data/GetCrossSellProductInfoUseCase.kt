package com.hedvig.android.feature.product.inapp.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import octopus.CrossSellProductInfoQuery

internal interface GetCrossSellProductInfoUseCase {
  suspend fun invoke(crossSellId: String): Either<ApolloOperationError, CrossSellProductInfo>
}

@ContributesBinding(AppScope::class)
@Inject
internal class GetCrossSellProductInfoUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCrossSellProductInfoUseCase {
  override suspend fun invoke(crossSellId: String): Either<ApolloOperationError, CrossSellProductInfo> {
    return apolloClient
      .query(CrossSellProductInfoQuery(crossSellId))
      .safeExecute()
      .map { data -> data.currentMember.crossSellProductInfo.toProductInfo() }
      .onLeft { error ->
        logcat(operationError = error) { "GetCrossSellProductInfoUseCase failed for crossSellId:$crossSellId" }
      }
  }
}

private fun CrossSellProductInfoQuery.Data.CurrentMember.CrossSellProductInfo.toProductInfo(): CrossSellProductInfo {
  return CrossSellProductInfo(
    heroImage = ImageAsset(id = heroImage.id, src = heroImage.src, description = heroImage.alt),
    heroIntro = heroIntro?.let { CrossSellProductInfo.HeroIntro(title = it.title, description = it.description) },
    usps = usps.map { CrossSellProductInfo.Usp(title = it.title, description = it.description) },
    carousel = carousel.map { item ->
      CrossSellProductInfo.CarouselItem(
        title = item.title,
        subtitle = item.subtitle,
        image = ImageAsset(id = item.image.id, src = item.image.src, description = item.image.alt),
        videoUrl = item.videoUrlWebm,
        loop = item.loop,
      )
    },
  )
}
