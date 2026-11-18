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
   * [image] is a still image or an animated WebP.
   */
  data class CarouselItem(
    val title: String,
    val subtitle: String?,
    val image: ImageAsset,
  )
}
