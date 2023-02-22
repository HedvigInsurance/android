package com.hedvig.android.hanalytics.featureflags.loginmethod

import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.hanalytics.LoginMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class DevLoginMethodProvider(
  private val marketManager: MarketManager,
) : LoginMethodProvider {
  override suspend fun getLoginMethod(): LoginMethod {
    return marketManager.observeMarket().first().let { market ->
      when (market) {
        Market.SE -> LoginMethod.BANK_ID_SWEDEN
        Market.NO -> LoginMethod.BANK_ID_NORWAY
        Market.DK -> LoginMethod.NEM_ID
        Market.FR -> throw IllegalArgumentException()
        null -> LoginMethod.BANK_ID_SWEDEN
      }
    }
  }
}
