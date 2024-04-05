package com.hedvig.android.app.ui

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.hedvig.android.feature.forever.navigation.ForeverDestination
import com.hedvig.android.feature.home.home.navigation.HomeDestination
import com.hedvig.android.feature.insurances.navigation.InsurancesDestination
import com.hedvig.android.feature.payments.navigation.PaymentsDestination
import com.hedvig.android.feature.profile.navigation.ProfileDestination
import com.hedvig.android.navigation.core.TopLevelGraph
import kotlinx.serialization.serializer

/**
 * Checks if the [TopLevelGraph] is part of the hiararchy of [this]
 */
internal fun NavDestination?.isTopLevelGraphInHierarchy(topLevelGraph: TopLevelGraph): Boolean {
  val hierarchy = this?.hierarchy ?: return false
  val topLevelGraphId = when (topLevelGraph) {
    TopLevelGraph.Home -> serializer<HomeDestination.Graph>().hashCode()
    TopLevelGraph.Insurances -> serializer<InsurancesDestination.Graph>().hashCode()
    TopLevelGraph.Forever -> serializer<ForeverDestination.Graph>().hashCode()
    TopLevelGraph.Payments -> serializer<PaymentsDestination.Graph>().hashCode()
    TopLevelGraph.Profile -> serializer<ProfileDestination.Graph>().hashCode()
  }
  return hierarchy.any { navDestination ->
    navDestination.id == topLevelGraphId
  }
}
