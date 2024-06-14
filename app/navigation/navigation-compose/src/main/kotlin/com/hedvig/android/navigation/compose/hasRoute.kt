package com.hedvig.android.navigation.compose

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlin.reflect.KClass

inline fun <reified T : Destination> NavDestination.typedHasRoute() = hasRoute<T>()

fun <T : Destination> NavDestination.typedHasRoute(route: KClass<T>) = hasRoute(route)
