@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.hedvig.android.feature.cancelinsurance.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.hedvig.android.feature.cancelinsurance.InsuranceId
import com.hedvig.android.feature.cancelinsurance.navigation.Destinations
import com.hedvig.android.feature.cancelinsurance.navigation.cancelInsuranceGraph
import com.kiwi.navigationcompose.typed.createRoutePattern

@Composable
internal fun CancelInsuranceNavHost(
  windowSizeClass: WindowSizeClass,
  navController: NavHostController,
  insuranceId: InsuranceId,
) {
  NavHost(
    navController,
    startDestination = createRoutePattern<Destinations.CancelInsurance>(),
  ) {
    cancelInsuranceGraph(
      windowSizeClass,
      navController,
      insuranceId,
    )
  }
}
