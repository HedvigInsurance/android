package com.hedvig.android.navigation.core

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.navOptions
import com.kiwi.navigationcompose.typed.Destination

interface Navigator {
  fun NavBackStackEntry.navigate(
    destination: Destination,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
  )

  fun NavBackStackEntry.navigate(destination: Destination, builder: NavOptionsBuilder.() -> Unit) {
    navigate(destination, navOptions(builder))
  }

  /**
   * This navigate event doesn't check if the current lifecycle state is resumed before navigating. Using this may mean
   * that double clicking something fast would trigger the navigation event more than once.
   */
  fun navigateUnsafe(
    destination: Destination,
    navOptions: NavOptions? = null,
    navigatorExtras: androidx.navigation.Navigator.Extras? = null,
  )

  fun navigateUp()

  fun popBackStack()
}
