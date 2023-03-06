package com.hedvig.android.feature.terminateinsurance.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.navigation.Destinations
import com.hedvig.android.feature.terminateinsurance.navigation.terminateInsuranceGraph
import com.kiwi.navigationcompose.typed.createRoutePattern

@Composable
internal fun TerminateInsuranceNavHost(
  windowSizeClass: WindowSizeClass,
  navController: NavHostController,
  insuranceId: InsuranceId,
  navigateUp: () -> Boolean,
  finishTerminationFlow: () -> Unit,
) {
  NavHost(
    navController,
    startDestination = createRoutePattern<Destinations.TerminateInsurance>(),
  ) {
    terminateInsuranceGraph(
      windowSizeClass = windowSizeClass,
      navController = navController,
      insuranceId = insuranceId,
      navigateUp = navigateUp,
      finishTerminationFlow = finishTerminationFlow,
    )
  }
}
