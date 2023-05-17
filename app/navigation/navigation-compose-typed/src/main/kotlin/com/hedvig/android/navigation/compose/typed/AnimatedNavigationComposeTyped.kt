package com.hedvig.android.navigation.compose.typed

import androidx.annotation.MainThread
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.createNavArguments
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.decodeArguments
import com.kiwi.navigationcompose.typed.registerDestinationType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import com.google.accompanist.navigation.animation.composable as accompanistComposable
import com.google.accompanist.navigation.animation.navigation as accompanistNavigation

inline fun <reified T : Destination> NavGraphBuilder.animatedNavigation(
  startDestination: String,
  deepLinks: List<NavDeepLink> = emptyList(),
  noinline enterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
  noinline exitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
  noinline popEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
  noinline popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
  noinline builder: NavGraphBuilder.() -> Unit,
) {
  animatedNavigation(
    kClass = T::class,
    serializer = serializer(),
    startDestination = startDestination,
    deepLinks = deepLinks,
    enterTransition = enterTransition,
    exitTransition = exitTransition,
    popEnterTransition = popEnterTransition,
    popExitTransition = popExitTransition,
    builder = builder,
  )
}

@MainThread
@PublishedApi
internal fun <T : Destination> NavGraphBuilder.animatedNavigation(
  kClass: KClass<T>,
  serializer: KSerializer<T>,
  startDestination: String,
  deepLinks: List<NavDeepLink> = emptyList(),
  enterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
  exitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
  popEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
  popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
  builder: NavGraphBuilder.() -> Unit,
) {
  registerDestinationType(kClass, serializer)
  accompanistNavigation(
    startDestination = startDestination,
    route = createRoutePattern(serializer),
    arguments = createNavArguments(serializer),
    deepLinks = deepLinks,
    enterTransition = enterTransition,
    exitTransition = exitTransition,
    popEnterTransition = popEnterTransition,
    popExitTransition = popExitTransition,
    builder = builder,
  )
}

@MainThread
inline fun <reified T : Destination> NavGraphBuilder.animatedComposable(
  deepLinks: List<NavDeepLink> = emptyList(),
  noinline enterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
  noinline exitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
  noinline popEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
  noinline popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
  noinline content: @Composable T.(NavBackStackEntry) -> Unit,
) {
  animatedComposable(
    kClass = T::class,
    serializer = serializer(),
    deepLinks = deepLinks,
    enterTransition = enterTransition,
    exitTransition = exitTransition,
    popEnterTransition = popEnterTransition,
    popExitTransition = popExitTransition,
    content = content,
  )
}

@PublishedApi
@MainThread
internal fun <T : Destination> NavGraphBuilder.animatedComposable(
  kClass: KClass<T>,
  serializer: KSerializer<T>,
  deepLinks: List<NavDeepLink> = emptyList(),
  enterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
  exitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
  popEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
  popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
  content: @Composable T.(NavBackStackEntry) -> Unit,
) {
  registerDestinationType(kClass, serializer)
  accompanistComposable(
    route = createRoutePattern(serializer),
    arguments = createNavArguments(serializer),
    deepLinks = deepLinks,
    enterTransition = enterTransition,
    exitTransition = exitTransition,
    popEnterTransition = popEnterTransition,
    popExitTransition = popExitTransition,
  ) { navBackStackEntry ->
    decodeArguments(serializer, navBackStackEntry).content(navBackStackEntry)
  }
}
