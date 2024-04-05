package com.hedvig.android.navigation.compose.typed

import androidx.navigation.compose.composable as androidxComposable
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.toRoute
import kotlin.reflect.KType

public inline fun <reified T : Any> NavGraphBuilder.composable(
  typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
  deepLinks: List<NavDeepLink> = emptyList(),
  noinline enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
  @JvmSuppressWildcards EnterTransition?)? = null,
  noinline exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
  @JvmSuppressWildcards ExitTransition?)? = null,
  noinline popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
  @JvmSuppressWildcards EnterTransition?)? = enterTransition,
  noinline popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
  @JvmSuppressWildcards ExitTransition?)? = exitTransition,
  noinline sizeTransform: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
  @JvmSuppressWildcards SizeTransform?)? = null,
  noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
  androidxComposable<T>(
    typeMap,
    deepLinks,
    enterTransition,
    exitTransition,
    popEnterTransition,
    popExitTransition,
    sizeTransform,
    content,
  )
}

public inline fun <reified T : Any> NavGraphBuilder.composable(
  typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
  deepLinks: List<NavDeepLink> = emptyList(),
  noinline enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
  @JvmSuppressWildcards EnterTransition?)? = null,
  noinline exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
  @JvmSuppressWildcards ExitTransition?)? = null,
  noinline popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
  @JvmSuppressWildcards EnterTransition?)? = enterTransition,
  noinline popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
  @JvmSuppressWildcards ExitTransition?)? = exitTransition,
  noinline sizeTransform: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
  @JvmSuppressWildcards SizeTransform?)? = null,
  noinline content: @Composable AnimatedContentScope.(NavBackStackEntry, T) -> Unit,
) {
  androidxComposable<T>(
    typeMap, deepLinks, enterTransition, exitTransition, popEnterTransition, popExitTransition, sizeTransform,
  ) { navBackStackEntry ->
    content(navBackStackEntry, navBackStackEntry.toRoute<T>())
  }
}
