package com.hedvig.app.util.featureflags.flags

import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager

class DevFeatureFlagProvider(
  private val marketManager: MarketManager,
) : FeatureFlagProvider {

  override suspend fun isFeatureEnabled(feature: Feature): Boolean {
    val isQasaMember = false
    return when (feature) {
      Feature.CONNECT_PAYMENT_POST_ONBOARDING -> marketManager.market == Market.SE
      Feature.COMMON_CLAIMS -> !isQasaMember
      Feature.CONNECT_PAYIN_REMINDER -> !isQasaMember
      Feature.EXTERNAL_DATA_COLLECTION -> marketManager.market == Market.SE
      Feature.FRANCE_MARKET -> true
      Feature.KEY_GEAR -> false
      Feature.MOVING_FLOW -> true
      Feature.QUOTE_CART -> false
      Feature.REFERRAL_CAMPAIGN -> false
      Feature.PAYMENT_SCREEN -> !isQasaMember
      Feature.REFERRALS -> !isQasaMember
      Feature.SHOW_CHARITY -> !isQasaMember
      Feature.UPDATE_NECESSARY -> false
    }
  }
}
