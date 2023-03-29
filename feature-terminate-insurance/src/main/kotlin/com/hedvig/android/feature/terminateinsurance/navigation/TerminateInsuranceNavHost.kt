package com.hedvig.android.feature.terminateinsurance.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.kiwi.navigationcompose.typed.createRoutePattern

@Composable
internal fun TerminateInsuranceNavHost(
  windowSizeClass: WindowSizeClass,
  navController: NavHostController,
  insuranceId: InsuranceId,
  insuranceDisplayName: String,
  openChat: () -> Unit,
  openPlayStore: () -> Unit,
  navigateUp: () -> Boolean,
  finishTerminationFlow: () -> Unit,
) {
  val density = LocalDensity.current
  AnimatedNavHost(
    navController = navController,
    startDestination = createRoutePattern<Destinations.TerminateInsurance>(),
  ) {
    terminateInsuranceGraph(
      windowSizeClass = windowSizeClass,
      density = density,
      navController = navController,
      insuranceId = insuranceId,
      insuranceDisplayName = insuranceDisplayName,
      navigateUp = navigateUp,
      openPlayStore = openPlayStore,
      openChat = openChat,
      finishTerminationFlow = finishTerminationFlow,
    )
  }
}
