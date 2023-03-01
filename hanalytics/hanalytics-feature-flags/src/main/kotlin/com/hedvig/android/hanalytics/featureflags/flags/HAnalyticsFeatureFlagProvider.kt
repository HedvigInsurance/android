package com.hedvig.android.hanalytics.featureflags.flags

import com.hedvig.hanalytics.ClaimType
import com.hedvig.hanalytics.HAnalytics

internal class HAnalyticsFeatureFlagProvider(
  private val hAnalytics: HAnalytics,
) : FeatureFlagProvider {
  override suspend fun isFeatureEnabled(feature: Feature): Boolean = when (feature) {
    Feature.COMMON_CLAIMS -> hAnalytics.homeCommonClaim()
    Feature.CONNECT_PAYIN_REMINDER -> hAnalytics.connectPaymentReminder()
    Feature.CONNECT_PAYMENT_POST_ONBOARDING -> hAnalytics.postOnboardingShowPaymentStep()
    Feature.EXTERNAL_DATA_COLLECTION -> hAnalytics.allowExternalDataCollection()
    Feature.FRANCE_MARKET -> hAnalytics.frenchMarket()
    Feature.MOVING_FLOW -> hAnalytics.movingFlow()
    Feature.PAYMENT_SCREEN -> hAnalytics.paymentScreen()
    Feature.QUOTE_CART -> hAnalytics.useQuoteCart()
    Feature.REFERRALS -> hAnalytics.forever()
    Feature.REFERRAL_CAMPAIGN -> hAnalytics.foreverFebruaryCampaign()
    Feature.SHOW_BUSINESS_MODEL -> hAnalytics.showCharity()
    Feature.TERMINATION_FLOW -> hAnalytics.terminationFlow()
    Feature.UPDATE_NECESSARY -> hAnalytics.updateNecessary()
    Feature.USE_ODYSSEY_CLAIM_FLOW -> {
      val useOdyssey = hAnalytics.odysseyClaims()
      hAnalytics.claimFlowType(if (useOdyssey) ClaimType.AUTOMATION else ClaimType.MANUAL)
      useOdyssey
    }
    Feature.USE_NATIVE_CLAIMS_FLOW -> false
  }
}
