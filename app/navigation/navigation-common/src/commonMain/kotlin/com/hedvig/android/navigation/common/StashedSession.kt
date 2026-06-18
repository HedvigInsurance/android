package com.hedvig.android.navigation.common

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

/**
 * The logged-in session captured at logout: the rendered [entries] plus all [parkedRuns], tagged
 * with the [memberId] (JWT `sub`) it belonged to. Held by the hoisted navigation state and excluded
 * from the live-content set so the retained decorators dispose every key's per-entry state while the
 * session sits stashed. Restored on a same-member login; persisted across process death.
 */
@Serializable
data class StashedSession(
  val memberId: String,
  val entries: List<@Polymorphic HedvigNavKey>,
  val parkedRuns: Map<TopLevelTab, List<@Polymorphic HedvigNavKey>>,
)
