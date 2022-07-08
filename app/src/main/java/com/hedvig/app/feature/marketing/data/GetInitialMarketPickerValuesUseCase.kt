package com.hedvig.app.feature.marketing.data

import android.content.Context
import arrow.core.identity
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.featureflags.flags.Feature

class GetInitialMarketPickerValuesUseCase(
  private val marketManager: MarketManager,
  private val context: Context,
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) {
  suspend operator fun invoke(): Pair<Market, Language?> {
    val currentMarket = marketManager.market
    if (currentMarket != null) {
      val currentLanguage = Language.fromSettings(context, currentMarket)
      return currentMarket to currentLanguage
    }

    val marketOrDefault = apolloClient
      .query(GeoQuery())
      .safeQuery()
      .toEither()
      .map { runCatching { Market.valueOf(it.geo.countryISOCode) }.getOrNull() }
      .map { market ->
        if (market == Market.FR && !featureManager.isFeatureEnabled(Feature.FRANCE_MARKET)) {
          null
        } else {
          market
        }
      }
      .fold({ null }, ::identity)
      ?: Market.SE

    return Pair(marketOrDefault, marketOrDefault.defaultLanguage())
  }
}
