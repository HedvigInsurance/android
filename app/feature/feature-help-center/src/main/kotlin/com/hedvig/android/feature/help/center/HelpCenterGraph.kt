package com.hedvig.android.feature.help.center

import androidx.compose.material3.Text
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestination
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestinations
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigation

fun NavGraphBuilder.helpCenterGraph(navigator: Navigator, hedvigDeepLinkContainer: HedvigDeepLinkContainer) {
  navigation<HelpCenterDestination>(
    startDestination = createRoutePattern<HelpCenterDestinations.HelpCenter>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.helpCenter },
    ),
  ) {
    composable<HelpCenterDestinations.HelpCenter> { backStackEntry ->
      Text("HelpCenterDestinations.HelpCenter")
    }
    composable<HelpCenterDestinations.Topic>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.helpCenterCommonTopic },
      ),
    ) { backStackEntry ->
      Text("HelpCenterDestinations.Topic:$id")
    }
    composable<HelpCenterDestinations.Question>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.helpCenterQuestion },
      ),
    ) { backStackEntry ->
      Text("HelpCenterDestinations.Question:$id")
    }
  }
}
