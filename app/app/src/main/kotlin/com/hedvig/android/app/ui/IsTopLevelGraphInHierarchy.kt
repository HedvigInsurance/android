package com.hedvig.android.app.ui

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.hedvig.android.feature.forever.navigation.ForeverDestination
import com.hedvig.android.feature.home.home.navigation.HomeDestination
import com.hedvig.android.feature.insurances.navigation.InsurancesDestination
import com.hedvig.android.feature.payments.navigation.PaymentsDestination
import com.hedvig.android.feature.profile.navigation.ProfileDestination
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.typedHasRoute
import com.hedvig.android.navigation.core.TopLevelGraph

/**
 * Checks if the [TopLevelGraph] is part of the hiararchy of [this]
 */
internal fun NavDestination?.isTopLevelGraphInHierarchy(topLevelGraph: TopLevelGraph): Boolean {
  val hierarchy = this?.hierarchy ?: return false
  return hierarchy.any { navDestination ->
    navDestination.typedHasRoute(topLevelGraph.destination::class)
  }
}

internal val TopLevelGraph.destination: Destination
  get() = when (this) {
    TopLevelGraph.Home -> HomeDestination.Graph
    TopLevelGraph.Insurances -> InsurancesDestination.Graph
    TopLevelGraph.Forever -> ForeverDestination.Graph
    TopLevelGraph.Payments -> PaymentsDestination.Graph
    TopLevelGraph.Profile -> ProfileDestination.Graph
  }
