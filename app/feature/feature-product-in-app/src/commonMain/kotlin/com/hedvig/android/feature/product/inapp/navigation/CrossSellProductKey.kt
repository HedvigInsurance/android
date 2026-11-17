package com.hedvig.android.feature.product.inapp.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

/**
 * The header fields and [storeUrl] come from the tapped cross-sell, so the page can show its header
 * before the product info has loaded. [storeUrl] is where the purchase continues. [discountText] is the
 * badge on the hero image, or null for no badge.
 */
@androidx.annotation.Keep
@Serializable
data class CrossSellProductKey(
  val crossSellId: String,
  val title: String,
  val subtitle: String,
  val pillowImageSrc: String,
  val storeUrl: String,
  val discountText: String?,
) : HedvigNavKey

/**
 * A bottom sheet for picking one of a generic cross-sell's [options] (for example dog or cat for pet), each of
 * which then opens its own product page.
 */
@androidx.annotation.Keep
@Serializable
data class CrossSellOptionsKey(
  val options: List<CrossSellProductKey>,
) : HedvigNavKey
