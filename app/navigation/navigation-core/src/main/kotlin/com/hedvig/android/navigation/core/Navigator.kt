package com.hedvig.android.navigation.core

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.navOptions

interface Navigator {
  fun <T : Any> NavBackStackEntry.navigate(
    destination: T,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
  )

  fun <T : Any> NavBackStackEntry.navigate(destination: T, builder: NavOptionsBuilder.() -> Unit) {
    navigate(destination, navOptions(builder))
  }

  /**
   * This navigate event doesn't check if the current lifecycle state is resumed before navigating. Using this may mean
   * that double clicking something fast would trigger the navigation event more than once.
   */
  fun <T : Any> navigateUnsafe(
    destination: T,
    navOptions: NavOptions? = null,
    navigatorExtras: androidx.navigation.Navigator.Extras? = null,
  )

  fun navigateUp()

  fun popBackStack()
}
