package com.hedvig.android.feature.product.inapp.data

import com.hedvig.android.data.contract.ImageAsset

internal data class CrossSellProductInfo(
  val heroImage: ImageAsset,
  val heroIntro: HeroIntro?,
  val usps: List<Usp>,
  val carousel: List<CarouselItem>,
) {
  data class HeroIntro(
    val title: String,
    val description: String,
  )

  data class Usp(
    val title: String,
    val description: String,
  )

  /**
   * [image] is the slide itself, or the poster shown under [videoUrl] while a video slide loads. A video
   * slide with [loop] false plays once and holds its last frame.
   */
  data class CarouselItem(
    val title: String,
    val subtitle: String?,
    val image: ImageAsset,
    val videoUrl: String?,
    val loop: Boolean,
  )
}
