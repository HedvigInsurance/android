package com.hedvig.android.hanalytics.featureflags.flags

import com.hedvig.hanalytics.ClaimType
import com.hedvig.hanalytics.HAnalytics

internal class HAnalyticsFeatureFlagProvider(
  private val hAnalytics: HAnalytics,
) : FeatureFlagProvider {
  // todo remember to reflect these options inside unleashed for the Android client
  //  Feature.MOVING_FLOW -> marketManager.market == Market.SE || marketManager.market == Market.NO
  //  Feature.CONNECT_PAYMENT_AT_SIGN -> marketManager.market == Market.NO || marketManager.market == Market.DK
  override suspend fun isFeatureEnabled(feature: Feature): Boolean = when (feature) {
    Feature.CONNECT_PAYMENT_POST_ONBOARDING -> hAnalytics.postOnboardingShowPaymentStep()
    Feature.EXTERNAL_DATA_COLLECTION -> hAnalytics.allowExternalDataCollection()
    Feature.FRANCE_MARKET -> hAnalytics.frenchMarket()
    Feature.MOVING_FLOW -> hAnalytics.movingFlow()
    Feature.QUOTE_CART -> hAnalytics.useQuoteCart()
    Feature.CONNECT_PAYIN_REMINDER -> hAnalytics.connectPaymentReminder()
    Feature.COMMON_CLAIMS -> hAnalytics.homeCommonClaim()
    Feature.PAYMENT_SCREEN -> hAnalytics.paymentScreen()
    Feature.REFERRAL_CAMPAIGN -> hAnalytics.foreverFebruaryCampaign()
    Feature.REFERRALS -> hAnalytics.forever()
    Feature.SHOW_BUSINESS_MODEL -> hAnalytics.showCharity()
    Feature.UPDATE_NECESSARY -> hAnalytics.updateNecessary()
    Feature.USE_ODYSSEY_CLAIM_FLOW -> {
      val useOdyssey = hAnalytics.odysseyClaims()
      hAnalytics.claimFlowType(if (useOdyssey) ClaimType.AUTOMATION else ClaimType.MANUAL)
      useOdyssey
    }
  }
}
