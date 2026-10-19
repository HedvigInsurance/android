package com.hedvig.android.feature.inapppurchase.navigation

import com.hedvig.android.data.contract.InAppProduct
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

/**
 * The start of the in-app purchase flow for [product].
 */
@Serializable
data class InAppPurchaseKey(val product: InAppProduct) : HedvigNavKey
