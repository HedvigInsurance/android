package com.hedvig.android.feature.editcoinsured.navigation

import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.DeepLinkMatcherProvider
import com.hedvig.android.navigation.compose.uriDeepLinkMatchers
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
@Inject
internal class EditCoInsuredDeepLinkMatcherProvider(
  private val container: HedvigDeepLinkContainer,
) : DeepLinkMatcherProvider {
  override fun matchers(): List<DeepLinkMatcher<out HedvigNavKey>> = uriDeepLinkMatchers(
    container.editCoInsured + container.editCoInsuredWithoutContractId,
    EditCoInsuredTriageKey.serializer(),
  ) +
    uriDeepLinkMatchers(container.editCoOwners, EditCoOwnersTriageDeepLinkKey.serializer())
}
