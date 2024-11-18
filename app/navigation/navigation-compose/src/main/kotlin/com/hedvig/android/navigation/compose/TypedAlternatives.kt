package com.hedvig.android.navigation.compose

import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.PopUpToBuilder
import com.hedvig.android.navigation.common.Destination
import kotlin.reflect.KClass

inline fun <reified T : Destination> NavController.typedPopBackStack(
  inclusive: Boolean,
  saveState: Boolean = false,
): Boolean = popBackStack<T>(inclusive, saveState)

inline fun <reified T : Destination> NavOptionsBuilder.typedPopUpTo(
  noinline popUpToBuilder: PopUpToBuilder.() -> Unit = {},
) = popUpTo<T>(popUpToBuilder)

inline fun <reified T : Destination> NavDestination.typedHasRoute() = hasRoute<T>()

fun <T : Destination> NavDestination.typedHasRoute(route: KClass<T>) = hasRoute(route)
