package com.hedvig.android.navigation.compose

import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import com.hedvig.android.navigation.common.HedvigNavKey

/**
 * Each feature contributes one [DeepLinkMatcherProvider] into the Metro `AppScope` graph via `@ContributesIntoSet`.
 * `:app` injects the resulting `Set<DeepLinkMatcherProvider>`, flattens every [matchers] list, and feeds the result
 * into [HedvigDeepLinkMatcher]. This lets each feature own its URI → destination mapping without a central aggregator
 * that has to be edited (and can be forgotten) for every new feature.
 */
interface DeepLinkMatcherProvider {
  fun matchers(): List<DeepLinkMatcher<out HedvigNavKey>>
}
