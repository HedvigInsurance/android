package com.hedvig.android.hanalytics.featureflags.flags

import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager

internal class DevFeatureFlagProvider(
  private val marketManager: MarketManager,
) : FeatureFlagProvider {

  override suspend fun isFeatureEnabled(feature: Feature): Boolean {
    val isQasaMember = false
    return when (feature) {
      Feature.COMMON_CLAIMS -> !isQasaMember
      Feature.CONNECT_PAYIN_REMINDER -> !isQasaMember
      Feature.CONNECT_PAYMENT_POST_ONBOARDING -> marketManager.market == Market.SE
      Feature.EXTERNAL_DATA_COLLECTION -> marketManager.market == Market.SE
      Feature.FRANCE_MARKET -> true
      Feature.MOVING_FLOW -> true
      Feature.PAYMENT_SCREEN -> !isQasaMember
      Feature.QUOTE_CART -> false
      Feature.REFERRALS -> !isQasaMember
      Feature.REFERRAL_CAMPAIGN -> false
      Feature.SHOW_BUSINESS_MODEL -> !isQasaMember
      Feature.TERMINATION_FLOW -> true
      Feature.UPDATE_NECESSARY -> false
      Feature.USE_ODYSSEY_CLAIM_FLOW -> true
      Feature.USE_NATIVE_CLAIMS_FLOW -> false
    }
  }
}
