package com.hedvig.android.app.urihandler

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher
import com.hedvig.android.navigation.compose.uriDeepLinkMatchers
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Recognizes the `submit-claim` deep link specifically. The regular Home deep link and the
 * submit-claim deep link both resolve to [HomeKey], so the matched key alone cannot tell them apart;
 * this matches only the [HedvigDeepLinkContainer.claimFlow] patterns so the caller can additionally
 * raise [com.hedvig.android.feature.home.home.StartClaimSheetSignal] without affecting navigation.
 */
@Inject
@SingleIn(AppScope::class)
internal class StartClaimDeepLinkRecognizer(
  container: HedvigDeepLinkContainer,
) {
  private val matcher = HedvigDeepLinkMatcher(uriDeepLinkMatchers(container.claimFlow, HomeKey.serializer()))

  fun isStartClaim(uri: String): Boolean = matcher.match(uri) != null
}
