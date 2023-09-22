package com.hedvig.app.feature.marketing.data

import arrow.core.merge
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import giraffe.GeoQuery

class GetInitialMarketPickerValuesUseCase(
  private val apolloClient: ApolloClient,
  private val marketManager: MarketManager,
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
      .mapLeft { null }
      .merge()
      ?: Market.SE

    return marketOrDefault to marketOrDefault.defaultLanguage()
  }
}
