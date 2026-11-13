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
      )
    },
  )
}

private fun fakeCrossSellProductInfo(crossSellId: String): CrossSellProductInfo {
  val petPillow = "https://a.storyblok.com/f/165473/832x832/c89ddd009f/pet-pillow-832x832px.png"
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
    carousel = fakeCarouselWebps(crossSellId).map { fileName ->
      CrossSellProductInfo.CarouselItem(
        title = fileName,
        subtitle = null,
        image = ImageAsset(id = fileName, src = FAKE_CAROUSEL_WEBP_DIR + fileName, description = ""),
      )
    },
  )
}

// TODO: Temporary. Copy the files into the app's internal storage with
//  adb push carousel-webp/. /data/local/tmp/carousel-webp/
//  adb shell run-as com.hedvig.dev.app sh -c 'mkdir -p files/carousel-webp && cp /data/local/tmp/carousel-webp/*.webp files/carousel-webp/'
private const val FAKE_CAROUSEL_WEBP_DIR = "file:///data/user/0/com.hedvig.dev.app/files/carousel-webp/"

private fun fakeCarouselWebps(crossSellId: String): List<String> = when (crossSellId) {
  "crossSell:PET", "crossSell:PET_DOG" -> listOf(
    "firstvet_stop.webp",
    "hogt_forsakringsbelopp_stop.webp",
    "ingen_bindningstid_loop.webp",
    "TR-FORL-stop-ny.webp",
    "anmal_skada_stop.webp",
    "rykte_loop.webp",
  )

  "crossSell:PET_CAT" -> listOf(
    "TR-FORL-stop-ny.webp",
    "ingen_bindningstid_loop.webp",
    "firstvet_stop.webp",
    "anmal_skada_stop.webp",
    "OOO-stop-ny.webp",
  )

  "crossSell:CAR" -> listOf(
    "valfri_verkstad_stop.webp",
    "vagassistens_loop.webp",
    "allt_samlat_stop.webp",
    "under_25_stop.webp",
    "rykte_loop.webp",
  )

  "crossSell:ACCIDENT" -> listOf(
    "extremsport_loop.webp",
    "hela_varlden_stop.webp",
    "rykte_loop.webp",
    "hjalp-direkt-i-appen-loop-ny.webp",
    "ingen_bindningstid_loop.webp",
  )

  "crossSell:HOME" -> listOf(
    "switch_stop.webp",
    "ingen_bindningstid_loop.webp",
    "id-skydd-stop-ny.webp",
    "forever-stop-ny.webp",
    "flytt_stop.webp",
    "hjalp-direkt-i-appen-loop-ny.webp",
  )

  else -> listOf(
    "OOO-stop-ny.webp",
    "TR-FORL-stop-ny.webp",
    "allt_samlat_stop.webp",
    "anmal_skada_stop.webp",
    "drulle-stop-ny.webp",
    "extremsport_loop.webp",
    "firstvet_stop.webp",
    "flytt_stop.webp",
    "forever-stop-ny.webp",
    "hela_varlden_stop.webp",
    "hjalp-direkt-i-appen-loop-ny.webp",
    "hogt_forsakringsbelopp_stop.webp",
    "hussvamp-stop-ny.webp",
    "id-skydd-stop-ny.webp",
    "ingen_bindningstid_loop.webp",
    "rykte_loop.webp",
    "streaming_sv_v2.webp",
    "switch_stop.webp",
    "under_25_stop.webp",
    "vagassistens_loop.webp",
    "valfri_verkstad_stop.webp",
  )
}
