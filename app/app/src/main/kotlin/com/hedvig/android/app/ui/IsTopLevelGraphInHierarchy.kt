package com.hedvig.android.app.ui

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.hedvig.android.feature.forever.navigation.ForeverDestination
import com.hedvig.android.feature.home.home.navigation.HomeDestination
import com.hedvig.android.feature.insurances.navigation.InsurancesDestination
import com.hedvig.android.feature.payments.navigation.PaymentsDestination
import com.hedvig.android.feature.profile.navigation.ProfileDestination
import com.hedvig.android.navigation.core.TopLevelGraph
import com.kiwi.navigationcompose.typed.createRoutePattern

/**
 * Checks if the [TopLevelGraph] is part of the hiararchy of [this]
 */
internal fun NavDestination?.isTopLevelGraphInHierarchy(topLevelGraph: TopLevelGraph): Boolean {
  val topLevelGraphRelatedRoute = when (topLevelGraph) {
    TopLevelGraph.Home -> createRoutePattern<HomeDestination.Graph>()
    TopLevelGraph.Insurances -> createRoutePattern<InsurancesDestination.Graph>()
    TopLevelGraph.Forever -> createRoutePattern<ForeverDestination.Graph>()
    TopLevelGraph.Payments -> createRoutePattern<PaymentsDestination.Graph>()
    TopLevelGraph.Profile -> createRoutePattern<ProfileDestination.Graph>()
  }
  return this?.hierarchy?.any {
    it.route?.contains(topLevelGraphRelatedRoute) ?: false
  } ?: false
}
