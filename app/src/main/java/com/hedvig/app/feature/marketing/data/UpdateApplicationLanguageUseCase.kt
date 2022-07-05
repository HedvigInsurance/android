package com.hedvig.app.feature.marketing.data

import android.content.Context
import com.hedvig.app.feature.marketpicker.LocaleBroadcastManager
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager

class UpdateApplicationLanguageUseCase(
  private val marketManager: MarketManager,
  private val localeBroadcastManager: LocaleBroadcastManager,
  private val context: Context,
) {
  operator fun invoke(market: Market, language: Language) {
    marketManager.market = market
    Language.persist(context, language)
    localeBroadcastManager.sendBroadcast()
  }
}
