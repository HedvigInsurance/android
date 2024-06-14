package com.hedvig.android.navigation.compose

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import kotlin.reflect.KClass

private typealias EnterTransitionFactory =
  @JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?

private typealias ExitTransitionFactory =
  @JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?

private typealias SizeTransformFactory =
  AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards SizeTransform?

inline fun <reified T : Destination> NavGraphBuilder.navdestination(
  destinationNavTypeAware: DestinationNavTypeAware = NoOpDestinationNavTypeAware,
  deepLinks: List<NavDeepLink> = emptyList(),
  noinline enterTransition: EnterTransitionFactory? = null,
  noinline exitTransition: ExitTransitionFactory? = null,
  noinline popEnterTransition: EnterTransitionFactory? = enterTransition,
  noinline popExitTransition: ExitTransitionFactory? = exitTransition,
  noinline sizeTransform: SizeTransformFactory? = null,
  noinline content: @Composable T.(NavBackStackEntry) -> Unit,
) {
  composable<T>(
    typeMap = typeMapOf(destinationNavTypeAware.typeList),
    deepLinks = deepLinks,
    enterTransition = enterTransition,
    exitTransition = exitTransition,
    popEnterTransition = popEnterTransition,
    popExitTransition = popExitTransition,
    sizeTransform = sizeTransform,
    content = { navBackStackEntry ->
      CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
        val destination = navBackStackEntry.toRoute<T>()
        destination.content(navBackStackEntry)
      }
    },
  )
}

inline fun <reified T : Destination> NavGraphBuilder.navgraph(
  startDestination: KClass<out Destination>,
  destinationNavTypeAware: DestinationNavTypeAware = NoOpDestinationNavTypeAware,
  deepLinks: List<NavDeepLink> = emptyList(),
  noinline enterTransition: EnterTransitionFactory? = null,
  noinline exitTransition: ExitTransitionFactory? = null,
  noinline popEnterTransition: EnterTransitionFactory? = enterTransition,
  noinline popExitTransition: ExitTransitionFactory? = exitTransition,
  noinline sizeTransform: SizeTransformFactory? = null,
  noinline builder: NavGraphBuilder.() -> Unit,
) {
  navigation<T>(
    startDestination = startDestination,
    typeMap = typeMapOf(destinationNavTypeAware.typeList),
    deepLinks = deepLinks,
    enterTransition = enterTransition,
    exitTransition = exitTransition,
    popEnterTransition = popEnterTransition,
    popExitTransition = popExitTransition,
    sizeTransform = sizeTransform,
    builder = builder,
  )
}
