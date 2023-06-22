package com.hedvig.android.navigation.core

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.kiwi.navigationcompose.typed.Destination

interface Navigator {
  fun NavBackStackEntry.navigate(
    destination: Destination,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
  )

  fun navigateUp()
}
