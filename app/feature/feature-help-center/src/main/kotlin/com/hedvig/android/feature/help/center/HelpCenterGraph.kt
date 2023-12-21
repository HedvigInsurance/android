package com.hedvig.android.feature.help.center

import androidx.compose.material3.Text
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestination
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestinations
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigation

fun NavGraphBuilder.helpCenterGraph(navigator: Navigator) {
  navigation<HelpCenterDestination>(
    startDestination = createRoutePattern<HelpCenterDestinations.HelpCenter>(),
  ) {
    composable<HelpCenterDestinations.HelpCenter> { backStackEntry ->
      Text("HelpCenterDestinations.HelpCenter")
    }
    composable<HelpCenterDestinations.Topic> { backStackEntry ->
      Text("HelpCenterDestinations.Topic")
    }
    composable<HelpCenterDestinations.Question> { backStackEntry ->
      Text("HelpCenterDestinations.Question")
    }
  }
}
