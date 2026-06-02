package com.hedvig.android.feature.connect.payment.trustly.navigation

import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyDestination
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.DeepLinkMatcherProvider
import com.hedvig.android.navigation.compose.uriDeepLinkMatchers
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
@Inject
internal class TrustlyDeepLinkMatcherProvider(
  private val container: HedvigDeepLinkContainer,
) : DeepLinkMatcherProvider {
  override fun matchers(): List<DeepLinkMatcher<out Destination>> =
    uriDeepLinkMatchers(container.connectPayment + container.directDebit, TrustlyDestination.serializer())
}
