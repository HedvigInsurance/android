package com.hedvig.android.feature.product.inapp.data

import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.data.contract.InAppProduct

internal data class CrossSellProductInfo(
  val heroImage: ImageAsset,
  val heroIntro: HeroIntro?,
  val usps: List<Usp>,
  val carousel: List<CarouselItem>,
  /**
   * Null when the product can't be bought in the app, so the purchase continues on the web.
   */
  val inAppProduct: InAppProduct?,
  val faq: List<FaqItem>,
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

  data class FaqItem(
    val question: String,
    val answerMarkdown: String,
  )
}
