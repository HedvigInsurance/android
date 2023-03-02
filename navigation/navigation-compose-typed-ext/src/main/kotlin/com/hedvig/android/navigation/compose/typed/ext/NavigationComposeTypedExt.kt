@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.hedvig.android.navigation.compose.typed.ext

import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavArgumentBuilder
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.PopUpToBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.decodeArguments
import com.kiwi.navigationcompose.typed.internal.isNavTypeOptional
import com.kiwi.navigationcompose.typed.internal.toRoute
import com.kiwi.navigationcompose.typed.registerDestinationType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@ExperimentalSerializationApi
@MainThread
inline fun <reified T : Destination> NavGraphBuilder.composable(
  deepLinks: List<NavDeepLink> = emptyList(),
  extraNavArgumentConfiguration: Map<String, NavArgumentBuilder.() -> Unit> = emptyMap(),
  noinline content: @Composable T.(NavBackStackEntry) -> Unit,
) {
  composable(
    kClass = T::class,
    serializer = serializer(),
    deepLinks = deepLinks,
    extraNavArgumentConfiguration = extraNavArgumentConfiguration,
    content = content,
  )
}

@ExperimentalSerializationApi
@MainThread
@PublishedApi
internal fun <T : Destination> NavGraphBuilder.composable(
  kClass: KClass<T>,
  serializer: KSerializer<T>,
  deepLinks: List<NavDeepLink> = emptyList(),
  extraNavArgumentConfiguration: Map<String, NavArgumentBuilder.() -> Unit>,
  content: @Composable T.(NavBackStackEntry) -> Unit,
) {
  registerDestinationType(kClass, serializer)
  composable(
    route = createRoutePattern(serializer),
    arguments = createNavArguments(serializer, extraNavArgumentConfiguration),
    deepLinks = deepLinks,
  ) { navBackStackEntry ->
    decodeArguments(serializer, navBackStackEntry).content(navBackStackEntry)
  }
}

@ExperimentalSerializationApi
private fun createNavArguments(
  serializer: KSerializer<*>,
  extraNavArgumentConfiguration: Map<String, NavArgumentBuilder.() -> Unit>,
): List<NamedNavArgument> {
  return List(serializer.descriptor.elementsCount) { i ->
    val name = serializer.descriptor.getElementName(i)
    navArgument(name) {
      // Use StringType for all types to support nullability for all of them.
      type = NavType.StringType
      val isOptional = serializer.descriptor.isNavTypeOptional(i)
      nullable = isOptional
      // If something is optional, the default value is required.
      if (isOptional) {
        defaultValue = null
      }
      val extraConfiguration = extraNavArgumentConfiguration[name] ?: return@navArgument
      extraConfiguration()
    }
  }
}

@ExperimentalSerializationApi
@MainThread
fun NavOptionsBuilder.popUpTo(
  route: Destination,
  popUpToBuilder: PopUpToBuilder.() -> Unit = {},
) {
  popUpTo(route.toRoute(), popUpToBuilder)
}
