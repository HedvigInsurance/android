package com.hedvig.android.app.ui

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.hedvig.android.feature.forever.navigation.ForeverDestination
import com.hedvig.android.feature.home.home.navigation.HomeDestination
import com.hedvig.android.feature.insurances.navigation.InsurancesDestination
import com.hedvig.android.feature.payments.navigation.PaymentsDestination
import com.hedvig.android.feature.profile.navigation.ProfileDestination
import com.hedvig.android.navigation.core.TopLevelGraph

/**
 * Checks if the [TopLevelGraph] is part of the hiararchy of [this]
 */
internal fun NavDestination?.isTopLevelGraphInHierarchy(topLevelGraph: TopLevelGraph): Boolean {
  val hierarchy = this?.hierarchy ?: return false
  val topLevelGraphRelatedRoute = when (topLevelGraph) {
    TopLevelGraph.Home -> HomeDestination.Graph::class
    TopLevelGraph.Insurances -> InsurancesDestination.Graph::class
    TopLevelGraph.Forever -> ForeverDestination.Graph::class
    TopLevelGraph.Payments -> PaymentsDestination.Graph::class
    TopLevelGraph.Profile -> ProfileDestination.Graph::class
  }
  return hierarchy.any { navDestination ->
    navDestination.hasRoute(topLevelGraphRelatedRoute)
  }
}
