package com.hedvig.android.feature.inapppurchase.navigation

import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseOffer
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseProduct
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
internal data class InAppPurchaseProductInfoKey(val product: InAppPurchaseProduct) : HedvigNavKey

@Serializable
internal data class InAppPurchaseOffersKey(
  val productDisplayName: String,
  val shopSessionId: String,
  val offers: List<InAppPurchaseOffer>,
) : HedvigNavKey

@Serializable
internal data class InAppPurchaseSummaryKey(
  val productDisplayName: String,
  val shopSessionId: String,
  val offer: InAppPurchaseOffer,
) : HedvigNavKey

@Serializable
internal data class InAppPurchaseSuccessKey(val startDate: LocalDate?) : HedvigNavKey
