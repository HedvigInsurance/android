package com.hedvig.android.navigation.compose

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.PopUpToBuilder
import kotlin.reflect.KClass

inline fun <reified T : Destination> NavOptionsBuilder.typedPopUpTo(
  noinline popUpToBuilder: PopUpToBuilder.() -> Unit = {},
) = popUpTo<T>(popUpToBuilder)

inline fun <reified T : Destination> NavDestination.typedHasRoute() = hasRoute<T>()

fun <T : Destination> NavDestination.typedHasRoute(route: KClass<T>) = hasRoute(route)
