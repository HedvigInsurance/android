package com.hedvig.android.app.navigation

import com.datadog.android.rum.tracking.ComponentPredicate
import com.hedvig.android.navigation.common.AnalyticsNamed
import com.hedvig.android.navigation.common.HedvigNavKey

/**
 * Names a RUM view after the destination's pinned [AnalyticsNamed.analyticsName] when it has one.
 *
 * Returning null hands the naming back to Datadog, which falls back to the key's canonical class name.
 * Every destination is accepted, so the set of screens RUM reports is unchanged.
 */
internal object AnalyticsNamePredicate : ComponentPredicate<HedvigNavKey> {
  override fun accept(component: HedvigNavKey): Boolean = true

  override fun getViewName(component: HedvigNavKey): String? = (component as? AnalyticsNamed)?.analyticsName
}
