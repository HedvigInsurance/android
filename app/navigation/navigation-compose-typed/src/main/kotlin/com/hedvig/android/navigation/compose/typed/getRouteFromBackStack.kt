package com.hedvig.android.navigation.compose.typed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlinx.serialization.serializer

@Composable
inline fun <reified T : Destination> NavController.getRouteFromBackStack(backStackEntry: NavBackStackEntry): T {
  return remember(this, backStackEntry) {
    decodeArguments(serializer<T>(), getBackStackEntry(createRoutePattern<T>()))
  }
}
