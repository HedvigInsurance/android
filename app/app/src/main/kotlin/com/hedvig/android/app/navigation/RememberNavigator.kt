package com.hedvig.android.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.navigate

@Composable
internal fun rememberNavigator(navController: NavController, finishApp: () -> Unit): Navigator {
  val updatedFinishApp by rememberUpdatedState(finishApp)
  return remember(navController) {
    object : Navigator {
      override fun NavBackStackEntry.navigate(
        destination: Destination,
        navOptions: NavOptions?,
        navigatorExtras: androidx.navigation.Navigator.Extras?,
      ) {
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
          navigateUnsafe(destination, navOptions, navigatorExtras)
        }
      }

      override fun navigateUnsafe(
        destination: Destination,
        navOptions: NavOptions?,
        navigatorExtras: androidx.navigation.Navigator.Extras?,
      ) {
        navController.navigate(destination, navOptions, navigatorExtras)
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
