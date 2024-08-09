package com.hedvig.android.navigation.compose

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.PopUpToBuilder
import androidx.navigation.toRoute
import kotlin.reflect.KClass
import kotlin.reflect.KType

public inline fun <reified T : Any> NavOptionsBuilder.typedPopUpTo(
  noinline popUpToBuilder: PopUpToBuilder.() -> Unit = {},
) = popUpTo<T>(popUpToBuilder)

inline fun <reified T : Destination> NavDestination.typedHasRoute() = hasRoute<T>()

fun <T : Destination> NavDestination.typedHasRoute(route: KClass<T>) = hasRoute(route)

inline fun <reified T : Any> SavedStateHandle.typedToRoute(
  typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
): T = toRoute<T>(typeMap)
