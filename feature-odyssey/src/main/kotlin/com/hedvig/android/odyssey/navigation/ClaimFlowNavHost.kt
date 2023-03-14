package com.hedvig.android.odyssey.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavHostController
import coil.ImageLoader
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun ClaimFlowNavHost(
  windowSizeClass: WindowSizeClass,
  navController: NavHostController,
  imageLoader: ImageLoader,
  openChat: () -> Unit,
  navigateUp: () -> Boolean,
) {
  val density = LocalDensity.current
  // debug navigation
  LaunchedEffect(Unit) {
    navController.navigate(ClaimFlowDestination.AudioRecording)
    delay(2.seconds)
    navController.navigate(ClaimFlowDestination.ClaimSummary)
    delay(2.seconds)
    navController.navigate(ClaimFlowDestination.DateOfOccurrence)
    delay(2.seconds)
    navController.navigate(ClaimFlowDestination.DateOfOccurrencePlusLocation)
    delay(2.seconds)
    navController.navigate(ClaimFlowDestination.Location)
    delay(2.seconds)
    navController.navigate(ClaimFlowDestination.ManualHandling)
    delay(2.seconds)
    navController.navigate(ClaimFlowDestination.PhoneNumber)
    delay(2.seconds)
    navController.navigate(ClaimFlowDestination.SingleItem)
    delay(2.seconds)
    navController.navigate(ClaimFlowDestination.SingleItemPayout)
    delay(2.seconds)
    navController.navigate(ClaimFlowDestination.StartStep)
    delay(2.seconds)
    navController.navigate(ClaimFlowDestination.UnknownScreen)
    delay(2.seconds)
  }
  AnimatedNavHost(
    navController = navController,
    startDestination = createRoutePattern<Destinations.ClaimFlow>(),
  ) {
    claimFlowGraph(
      windowSizeClass = windowSizeClass,
      density = density,
      navController = navController,
      imageLoader = imageLoader,
      navigateUp = navigateUp,
      openChat = openChat,
      finishClaimFlow = { navigateUp() },
    )
  }
}
