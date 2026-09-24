package com.hedvig.android.feature.payin.account.navigation

import com.hedvig.android.navigation.common.DeepLinkAncestry
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab
import kotlinx.serialization.Serializable

@Serializable
data object PayinAccountKey : HedvigNavKey, DeepLinkAncestry {
  override val owningTab = TopLevelTab.Payments
  override val syntheticParents = emptyList<HedvigNavKey>()
}

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

/**
 * The Swish payin setup screen, reachable both from the payin flow and from onboarding.
 *
 * @param showSuccessScreen whether connecting ends on a confirmation the member dismisses. A caller
 *   that confirms the connection itself, as the onboarding step does, sets this false and gets the
 *   member handed straight back instead.
 */
@Serializable
data class SetupSwishPayinKey(
  val showSuccessScreen: Boolean = true,
) : HedvigNavKey, DeepLinkAncestry {
  override val owningTab = TopLevelTab.Payments
  override val syntheticParents = listOf(PayinAccountKey)
}

/** The picker for connecting a new payin method, seeded with what the member can and already has. */
@Serializable
data class SelectPayinMethodKey(
  val availableProviders: List<String>,
  val currentProviders: List<String>,
) : HedvigNavKey
