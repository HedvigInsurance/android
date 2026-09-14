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

/** The Swish payin setup screen, reachable both from the payin flow and from onboarding. */
@Serializable
data object SetupSwishPayinKey : HedvigNavKey

/** The picker for connecting a new payin method, seeded with what the member can and already has. */
@Serializable
data class SelectPayinMethodKey(
  val availableProviders: List<String>,
  val currentProviders: List<String>,
) : HedvigNavKey
