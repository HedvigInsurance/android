package com.hedvig.app.feature.marketing.data

import android.content.Context
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.app.feature.marketpicker.LocaleBroadcastManager

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
