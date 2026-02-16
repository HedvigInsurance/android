package com.hedvig.android.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.core.Navigator

@Composable
internal fun rememberNavigator(navController: NavController, finishApp: () -> Unit): Navigator {
  val updatedFinishApp by rememberUpdatedState(finishApp)
  return remember(navController) {
    object : Navigator {
      override fun navigate(destination: Destination, builder: NavOptionsBuilder.() -> Unit) {
        navController.navigate(destination, builder)
      }

      override fun navigateUp() {
        navController.navigateUp()
      }

      override fun popBackStack() {
        if (!navController.popBackStack()) {
          updatedFinishApp()
        }
      }
    }
  }
}
