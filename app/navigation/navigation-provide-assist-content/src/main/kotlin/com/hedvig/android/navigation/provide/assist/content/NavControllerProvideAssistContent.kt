package com.hedvig.android.navigation.provide.assist.content

import android.app.assist.AssistContent
import android.os.Bundle
import androidx.core.net.toUri
import androidx.navigation.NavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination

/**
 * To be used inside [android.app.Activity.onProvideAssistContent]. Uses the [NavController] to get the
 * [NavController.currentBackStackEntry]. From it, iterates over [allDeepLinkUriPatterns] and if the current
 * destination has a deep link that matches any of them, it tries to construct a web uri from it, combining the
 * deepLinkUriPatter along with the destination's nav arguments and the backStackEntry's arguments.
 *
 * In the recents screen, if the current destination has a deep link, the system allows the user to copy the URL of it.
 *
 * Context:
 *  [Recents URL sharing](https://developer.android.com/guide/components/activities/recents#url-sharing)
 *  [onProvideAssistContent](https://developer.android.com/reference/android/app/Activity#onProvideAssistContent(android.app.assist.AssistContent))
 */
fun NavController.provideAssistContent(outContent: AssistContent, allDeepLinkUriPatterns: List<String>) {
  val backStackEntry: NavBackStackEntry? = currentBackStackEntry
  val navDestination: NavDestination? = backStackEntry?.destination
  for (deepLinkUriPattern in allDeepLinkUriPatterns) {
    if (navDestination?.hasDeepLink(deepLinkUriPattern.toUri()) != true) {
      continue
    }
    val webUri: String? = constructWebUriOrNull(deepLinkUriPattern, backStackEntry, navDestination)
    if (webUri != null) {
      outContent.webUri = webUri.toUri()
      break
    }
  }
}

/**
 * For a given [deepLinkUriPattern], try to construct a uri with any placeholders inside it filled in. If there are no
 * placeholders, the uri is returned as-is.
 * Takes the [navDestination]'s [NavDestination.arguments], and for each one of them, tries to find the value that
 * exists inside the [backStackEntry]'s [NavBackStackEntry.arguments] which is for the same argument.
 * If there are placeholders, but not all of them can be filled in, we return null.
 * e.g. for "https://example.com/{id}/{name}?extra={extra}" and a [NavBackStackEntry.arguments] list like
 *  {id: "1", name: "John", extra: "extraValue"}, the result would be "https://example.com/1/John?extra=extraValue"
 */
private fun constructWebUriOrNull(
  deepLinkUriPattern: String,
  backStackEntry: NavBackStackEntry,
  navDestination: NavDestination,
): String? {
  if (!deepLinkUriPattern.contains("{")) {
    // If no parameters exist in the deep link, just use it as-is
    return deepLinkUriPattern
  }
  val backStackEntryArguments: Bundle = backStackEntry.arguments ?: return null
  val arguentNameToRealValueList: List<Pair<String, String?>> =
    navDestination.arguments.map { (argumentName: String, navArgument: NavArgument) ->
      val serializedTypeValue: String? =
        navArgument.type.serializeAsValue(navArgument.type.get(backStackEntryArguments, argumentName))
      argumentName to serializedTypeValue
    }
  val deepLinkWithPlaceholdersFilled =
    arguentNameToRealValueList.fold(initial = deepLinkUriPattern) { acc, (argumentName: String, value: String?) ->
      if (value == null) return@fold acc
      acc.replace("{$argumentName}", value)
    }
  return deepLinkWithPlaceholdersFilled.takeIf { !it.contains("{") }
}
