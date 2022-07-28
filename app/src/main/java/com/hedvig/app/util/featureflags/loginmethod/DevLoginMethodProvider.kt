package com.hedvig.app.util.featureflags.loginmethod

import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.hanalytics.LoginMethod

class DevLoginMethodProvider(
  private val marketManager: MarketManager,
) : LoginMethodProvider {
  override suspend fun getLoginMethod(): LoginMethod {
    return when (marketManager.market) {
      Market.SE -> LoginMethod.BANK_ID_SWEDEN
      Market.NO -> LoginMethod.BANK_ID_NORWAY
      Market.DK -> LoginMethod.NEM_ID
      Market.FR -> throw IllegalArgumentException()
      null -> LoginMethod.BANK_ID_SWEDEN
    }
  }
}
