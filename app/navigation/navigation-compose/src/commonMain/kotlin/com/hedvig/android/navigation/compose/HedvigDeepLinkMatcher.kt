package com.hedvig.android.navigation.compose

import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.KSerializer

/**
 * Aggregates every feature's [DeepLinkMatcher]s into one registry. Each feature navigation package exposes a public
 * function building its own matchers from the [com.hedvig.android.navigation.core.HedvigDeepLinkContainer], and :app
 * combines them all into a single [HedvigDeepLinkMatcher].
 *
 * Matching a URI returns the highest-priority [Destination] (per Nav3's [UriMatchResult] ordering), or null if no
 * matcher matches — in which case the caller should fall back to opening the URI in a browser.
 */
class HedvigDeepLinkMatcher(private val matchers: List<DeepLinkMatcher<out Destination>>) {
  fun match(uri: String): Destination? {
    val request = runCatching { DeepLinkRequest.fromUriString(uri) }.getOrNull() ?: return null
    var best: DeepLinkMatcher.MatchResult<out Destination>? = null
    matchers.forEach { matcher ->
      // A matcher for a destination with a required (no-default) argument throws when that argument is absent from the
      // URI, rather than returning null. Treat any throw as a non-match.
      val result = matcher.match(request) ?: return@forEach
      val currentBest = best
      if (currentBest == null || result.isHigherPriorityThan(currentBest)) {
        best = result
      }
    }
    return best?.key
  }
}

@Suppress("UNCHECKED_CAST")
private fun DeepLinkMatcher.MatchResult<out Destination>.isHigherPriorityThan(
  other: DeepLinkMatcher.MatchResult<out Destination>,
): Boolean = (this as DeepLinkMatcher.MatchResult<Destination>)
  .compareTo(other as DeepLinkMatcher.MatchResult<Destination>) > 0

/**
 * Builds [UriDeepLinkMatcher]s for the given [patterns], instantiating the destination [T] via its [serializer].
 * Each feature's deep-link function uses this to turn its [com.hedvig.android.navigation.core.HedvigDeepLinkContainer]
 * pattern lists into matchers.
 */
fun <T : Destination> uriDeepLinkMatchers(
  patterns: List<String>,
  serializer: KSerializer<T>,
): List<DeepLinkMatcher<out Destination>> = patterns.map { pattern ->
  UriDeepLinkMatcher(DeepLinkUri(pattern), serializer)
}
