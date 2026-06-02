package com.hedvig.android.app.ui

import com.hedvig.android.feature.forever.navigation.ForeverDestination
import com.hedvig.android.feature.home.home.navigation.HomeDestination
import com.hedvig.android.feature.insurances.navigation.InsurancesDestination
import com.hedvig.android.feature.payments.navigation.PaymentsDestination
import com.hedvig.android.feature.profile.navigation.ProfileDestination
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.core.TopLevelGraph

internal val TopLevelGraph.startDestination: Destination
  get() = when (this) {
    TopLevelGraph.Home -> HomeDestination.Home
    TopLevelGraph.Insurances -> InsurancesDestination.Insurances
    TopLevelGraph.Forever -> ForeverDestination.Forever
    TopLevelGraph.Payments -> PaymentsDestination.Payments
    TopLevelGraph.Profile -> ProfileDestination.Profile
  }
