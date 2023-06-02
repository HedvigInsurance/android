package com.hedvig.android.navigation.compose.typed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.createRoutePattern
import org.koin.androidx.compose.koinViewModel

/**
 * [Dest] is the destination to which the [ViewModel] will be scoped to. It is done so by getting the backStackEntry
 * from the backstack by looking up its route.
 */
@Composable
inline fun <reified Dest : Destination, reified VM : ViewModel> destinationScopedViewModel(
  navController: NavHostController,
  backStackEntry: NavBackStackEntry,
): VM {
  val parentEntry = remember(navController, backStackEntry) {
    navController.getBackStackEntry(createRoutePattern<Dest>())
  }
  return koinViewModel(viewModelStoreOwner = parentEntry)
}
