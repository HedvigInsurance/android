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
    Feature.DISABLE_CHAT -> hAnalytics.disableChat()
    Feature.DISABLE_DARK_MODE -> hAnalytics.disableDarkMode()
    Feature.EXTERNAL_DATA_COLLECTION -> hAnalytics.allowExternalDataCollection()
    Feature.MOVING_FLOW -> hAnalytics.movingFlow()
    Feature.PAYMENT_SCREEN -> hAnalytics.paymentScreen()
    Feature.QUOTE_CART -> hAnalytics.useQuoteCart()
    Feature.FOREVER -> hAnalytics.forever()
    Feature.REFERRAL_CAMPAIGN -> hAnalytics.foreverFebruaryCampaign()
    Feature.SHOW_BUSINESS_MODEL -> hAnalytics.showCharity()
    Feature.TERMINATION_FLOW -> hAnalytics.terminationFlow()
    Feature.UPDATE_NECESSARY -> hAnalytics.updateNecessary()
    Feature.USE_NATIVE_CLAIMS_FLOW -> {
      val useOdyssey = hAnalytics.claimsFlow()
      hAnalytics.claimFlowType(if (useOdyssey) ClaimType.AUTOMATION else ClaimType.MANUAL)
      useOdyssey
    }
    Feature.NEW_MOVING_FLOW -> false
    Feature.CLAIMS_TRIAGING -> hAnalytics.claimsTriaging()
    Feature.TRAVEL_CERTIFICATE -> hAnalytics.travelInsurance()
  }
}
