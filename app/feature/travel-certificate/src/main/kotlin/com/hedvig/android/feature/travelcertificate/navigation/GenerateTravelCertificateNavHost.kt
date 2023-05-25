package com.hedvig.android.feature.travelcertificate.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.kiwi.navigationcompose.typed.createRoutePattern

@Composable
internal fun GenerateTravelCertificateNavHost(
  navController: NavHostController,
  finish: () -> Unit,
) {
  val density = LocalDensity.current
  AnimatedNavHost(
    navController = navController,
    startDestination = createRoutePattern<Destinations.GenerateTravelCertificate>(),
  ) {
    generateTravelCertificateGraph(
      density = density,
      navController = navController,
      finish = finish,
    )
  }
}
