package com.hedvig.android.navigation.compose.typed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.toRoute

@Composable
inline fun <reified T : Any> NavController.getRouteFromBackStack(backStackEntry: NavBackStackEntry): T {
  return remember(this, backStackEntry) {
    getBackStackEntry<T>().toRoute<T>()
  }
}
