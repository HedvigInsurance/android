package com.feature.changeaddress.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.kiwi.navigationcompose.typed.createRoutePattern

@Composable
internal fun ChangeAddressNavHost(
  windowSizeClass: WindowSizeClass,
  navController: NavHostController,
  openChat: () -> Unit,
  navigateUp: () -> Boolean,
  finish: () -> Unit,
) {
  val density = LocalDensity.current
  AnimatedNavHost(
    navController = navController,
    startDestination = createRoutePattern<Destinations.ChangeAddress>(),
  ) {
    changeAddressGraph(
      windowSizeClass = windowSizeClass,
      density = density,
      navController = navController,
      openChat = openChat,
      navigateUp = navigateUp,
      finish = finish,
    )
  }
}
