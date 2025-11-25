package com.hedvig.android.navigation.core

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import com.hedvig.android.navigation.common.Destination

interface Navigator {
  fun NavBackStackEntry.navigate(destination: Destination, builder: NavOptionsBuilder.() -> Unit = {})

  /**
   * This navigate event doesn't check if the current lifecycle state is resumed before navigating. Using this may mean
   * that double clicking something fast would trigger the navigation event more than once.
   */
  fun navigateUnsafe(destination: Destination, builder: NavOptionsBuilder.() -> Unit = {})

  fun navigateUp()

  fun popBackStack()
}
