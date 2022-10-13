package com.hedvig.android.hanalytics.featureflags.flags

import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager

internal class DevFeatureFlagProvider(
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
      Feature.MOVING_FLOW -> true
      Feature.QUOTE_CART -> false
      Feature.REFERRAL_CAMPAIGN -> false
      Feature.PAYMENT_SCREEN -> !isQasaMember
      Feature.REFERRALS -> !isQasaMember
      Feature.SHOW_BUSINESS_MODEL -> !isQasaMember
      Feature.UPDATE_NECESSARY -> false
    }
  }
}
