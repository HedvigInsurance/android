package com.hedvig.android.feature.product.inapp.data

import arrow.core.Either
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.contract.ImageAsset
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
    // TODO: Temporary fake until the backend serves crossSellProductInfo. Restore the query below, and its
    //  safeExecute/logcat imports, before merging.
    return fakeCrossSellProductInfo(crossSellId).right()
    // return apolloClient
    //   .query(CrossSellProductInfoQuery(crossSellId))
    //   .safeExecute()
    //   .map { data -> data.currentMember.crossSellProductInfo.toProductInfo() }
    //   .onLeft { error ->
    //     logcat(operationError = error) { "GetCrossSellProductInfoUseCase failed for crossSellId:$crossSellId" }
    //   }
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

private fun fakeCrossSellProductInfo(crossSellId: String): CrossSellProductInfo {
  val petPillow = "https://a.storyblok.com/f/165473/832x832/c89ddd009f/pet-pillow-832x832px.png"
  val carPillow = "https://a.storyblok.com/f/165473/832x832/718f3fbae4/car-pillow-832x832px.png"
  return CrossSellProductInfo(
    heroImage = ImageAsset(id = "fake-hero", src = petPillow, description = ""),
    heroIntro = CrossSellProductInfo.HeroIntro(
      title = "Insurance that has your back",
      description = "Fake product info for $crossSellId. Sign up in minutes and cancel whenever you want.",
    ),
    usps = listOf(
      CrossSellProductInfo.Usp("Bundle and save", "Get a discount on every insurance you have with us."),
      CrossSellProductInfo.Usp("Claims in the app", "Report a claim in a few minutes, straight from your phone."),
      CrossSellProductInfo.Usp("No binding period", "Cancel at any time, with no extra fees."),
    ),
    carousel = listOf(
      CrossSellProductInfo.CarouselItem(
        title = "A looping video slide",
        subtitle = "Plays on repeat while shown",
        image = ImageAsset(id = "fake-slide-1", src = petPillow, description = ""),
        videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        loop = true,
      ),
      CrossSellProductInfo.CarouselItem(
        title = "An image slide",
        subtitle = null,
        image = ImageAsset(id = "fake-slide-2", src = carPillow, description = ""),
        videoUrl = null,
        loop = false,
      ),
    ),
  )
}
