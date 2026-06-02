package com.hedvig.android.feature.addon.purchase.navigation

import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.UriMatchResult
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.DeepLinkMatcherProvider
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
@Inject
internal class AddonPurchaseDeepLinkMatcherProvider(
  private val container: HedvigDeepLinkContainer,
) : DeepLinkMatcherProvider {
  override fun matchers(): List<DeepLinkMatcher<out Destination>> =
    (container.travelAddon + container.travelAddonWithContractId).map { pattern ->
      AddonDeepLinkMatcher(DeepLinkUri(pattern), AddonBannerSource.TRAVEL_DEEPLINK)
    } +
      (container.carAddon + container.carAddonWithContractId).map { pattern ->
        AddonDeepLinkMatcher(DeepLinkUri(pattern), AddonBannerSource.CAR_ADDON_DEEPLINK)
      }
}

/**
 * [TravelAddonTriage.source] is not part of the deep-link URI — it is derived from which path (travel vs car) matched.
 * The default [UriDeepLinkMatcher] decodes the key purely from the URI arguments via the serializer, which would fail
 * here because [TravelAddonTriage.source] has no default. So we construct the key ourselves from the fixed [source]
 * and the optional `contractId` query argument.
 */
private class AddonDeepLinkMatcher(
  uriPattern: DeepLinkUri,
  private val source: AddonBannerSource,
) : UriDeepLinkMatcher<TravelAddonTriage>(uriPattern, TravelAddonTriage.serializer()) {
  override fun matchArguments(
    pathArgs: Map<String, List<String>>,
    queryArgs: Map<String, List<String>>,
    fragmentArgs: Map<String, List<String>>,
  ): UriMatchResult<TravelAddonTriage> {
    val arguments = buildMap {
      putAll(fragmentArgs)
      putAll(queryArgs)
      putAll(pathArgs)
    }
    val contractId = arguments["contractId"]?.firstOrNull()
    return UriMatchResult(TravelAddonTriage(source = source, contractId = contractId), arguments)
  }
}
