package com.hedvig.app.feature.marketing.data

import arrow.core.identity
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.GeoQuery
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.app.LanguageService

class GetInitialMarketPickerValuesUseCase(
  private val marketManager: MarketManager,
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
  private val languageService: LanguageService,
) {
  suspend operator fun invoke(): Pair<Market, Language> {
    val currentMarket = marketManager.market
    if (currentMarket != null) {
      val currentLanguage = languageService.getLanguage()
      return currentMarket to currentLanguage
    }

    val marketOrDefault = apolloClient
      .query(GeoQuery())
      .safeExecute()
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

    return marketOrDefault to marketOrDefault.defaultLanguage()
  }
}
