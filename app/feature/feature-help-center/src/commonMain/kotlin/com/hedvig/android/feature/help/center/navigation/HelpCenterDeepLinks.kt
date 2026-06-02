package com.hedvig.android.feature.help.center.navigation

import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.DeepLinkMatcherProvider
import com.hedvig.android.navigation.compose.uriDeepLinkMatchers
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
@Inject
internal class HelpCenterDeepLinkMatcherProvider(
  private val container: HedvigDeepLinkContainer,
) : DeepLinkMatcherProvider {
  override fun matchers(): List<DeepLinkMatcher<out Destination>> =
    uriDeepLinkMatchers(container.helpCenter, HelpCenterDestinations.HelpCenter.serializer()) +
      uriDeepLinkMatchers(container.helpCenterCommonTopic, HelpCenterDestinations.Topic.serializer()) +
      uriDeepLinkMatchers(container.helpCenterQuestion, HelpCenterDestinations.Question.serializer()) +
      uriDeepLinkMatchers(container.puppyGuide, HelpCenterDestinations.PuppyGuide.serializer())
}
