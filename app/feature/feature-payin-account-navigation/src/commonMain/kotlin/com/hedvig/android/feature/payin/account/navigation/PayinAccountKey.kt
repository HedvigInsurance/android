package com.hedvig.android.feature.payin.account.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
data object PayinAccountKey : HedvigNavKey

/**
 * Names one of the member's connected payin methods. A method is identified by its provider because a member can
 * have at most one connected method per provider.
 */
@Serializable
enum class PayinMethodId {
  Trustly,
  Swish,
  Invoice,
}

@Serializable
data class PayinMethodDetailsKey(
  val method: PayinMethodId,
) : HedvigNavKey
