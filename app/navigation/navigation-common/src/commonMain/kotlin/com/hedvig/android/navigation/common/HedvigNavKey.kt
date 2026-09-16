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
 * A flow during which the Play Store review prompt must not be asked for. Unlike the other markers
 * here, this one is matched against **every** entry on the back stack rather than just the top: it
 * marks a flow that hosts other flows on top of itself, and it is those hosted flows that complete
 * and earn a review prompt. The prompt is held until the marked key leaves the stack, so the member
 * still gets asked, just not mid-flow.
 */
interface SuppressesAppStoreReviewRequest

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

/**
 * The identity under which a destination's saved state and retained `ViewModel` are held, and the
 * key both `BackstackController.allLiveContentKeys` and `owningTabByContentKey` are built from.
 *
 * Qualified by type, not just [toString]. Two destinations in different features can share a simple
 * name -- `SubmitFailureKey` exists in both `feature-choose-tier` and `feature-addon-purchase` -- and
 * a `data object`'s [toString] is only that name. Keying on [toString] alone would give them one
 * identity, which bleeds retained state between the two flows and makes `SaveableStateProvider` throw
 * "Key ... was used multiple times" if both are ever live at once.
 *
 * This deliberately does not reuse navigation3's own `defaultContentKey`. That function is
 * `@PublishedApi internal`, so it cannot be called from here, and it is not a stable contract: nav3
 * `1.2.0-rc01` changed it without a deprecation. More fundamentally, the controller has to produce
 * content keys for destinations that have no live `NavEntry` -- everything in `parkedRuns`, and
 * everything restored from `SavedStateRegistry` after process death -- so it cannot read them off
 * entries and must derive its own. Owning the derivation is what keeps the two sides in agreement.
 *
 * Entries get this applied centrally in `withHedvigContentKeys`, so individual `entry<>` call sites
 * never pass a `contentKey` themselves.
 *
 * The instance half comes from [toString], so a key must not override it with a value that is equal
 * for two distinct destinations of the same type: that would give both one identity and collapse
 * their retained state. The generated `data class` [toString] is always safe.
 */
fun HedvigNavKey.contentKey(): String = "${this::class.qualifiedName ?: this::class}/$this"
