package com.hedvig.android.navigation.common

import androidx.navigation3.runtime.NavKey

interface HedvigNavKey : NavKey

/**
 * A destination on which the cross-sell bottom sheet is allowed to appear after a member finishes a
 * flow (moving, edit co-insured, add/upgrade addon, change tier). Implemented by the screens a member
 * lands on at the end of those flows. Replaces the old per-feature
 * `xxxCrossSellBottomSheetPermittingDestinations` lists.
 */
interface CrossSellEligibleDestination

/**
 * A destination where an incoming chat push notification must be suppressed (the in-app screen shows
 * the new message itself). Replaces `listOfDestinationsWhichShouldNotShowChatNotification`.
 */
interface SuppressesChatPushNotification

/**
 * A destination from which reaching the logged-out state is treated as a deliberate "log me out now"
 * action, so the session is discarded rather than stashed for a same-member restore. Restoring the
 * nav back to this screen after a fresh login would be wrong. Replaces the dead
 * `destinationToExcludeFromSavingState`.
 */
interface DeliberateLogoutOrigin

/**
 * Opt-in override for the analytics parameters sent with a key's `screen_view` event. By default, the
 * tracker reflects a key's own serialized properties as parameters; a key implementing this takes full
 * control instead, returning exactly the parameters to attach. Use it when the serialized shape isn't
 * what you want reported — to rename, drop, derive, or coarsen high-cardinality values.
 */
interface TrackedScreen {
  val screenParameters: Map<String, Any?>
}
