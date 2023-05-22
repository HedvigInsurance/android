package com.hedvig.android.feature.travelcertificate.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateResult
import com.kiwi.navigationcompose.typed.createRoutePattern

@Composable
internal fun GenerateTravelCertificateNavHost(
  email: String?,
  travelCertificateSpecifications: TravelCertificateResult.TravelCertificateSpecifications,
  windowSizeClass: WindowSizeClass,
  navController: NavHostController,
  finish: () -> Unit,
) {
  val density = LocalDensity.current
  AnimatedNavHost(
    navController = navController,
    startDestination = createRoutePattern<Destinations.GenerateTravelCertificate>(),
  ) {
    generateTravelCertificateGraph(
      email = email,
      travelCertificateSpecifications = travelCertificateSpecifications,
      windowSizeClass = windowSizeClass,
      density = density,
      navController = navController,
      finish = finish,
    )
  }
}
