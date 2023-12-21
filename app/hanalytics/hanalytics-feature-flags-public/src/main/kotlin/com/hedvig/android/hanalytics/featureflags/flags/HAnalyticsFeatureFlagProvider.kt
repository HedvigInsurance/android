package com.hedvig.android.hanalytics.featureflags.flags

import com.hedvig.hanalytics.HAnalytics

internal class HAnalyticsFeatureFlagProvider(
  private val hAnalytics: HAnalytics,
) : FeatureFlagProvider {
  override suspend fun isFeatureEnabled(feature: Feature): Boolean = when (feature) {
    Feature.COMMON_CLAIMS -> hAnalytics.homeCommonClaim()
    Feature.CONNECT_PAYIN_REMINDER -> hAnalytics.connectPaymentReminder()
    Feature.CONNECT_PAYMENT_POST_ONBOARDING -> hAnalytics.postOnboardingShowPaymentStep()
    Feature.DISABLE_CHAT -> hAnalytics.disableChat()
    Feature.EXTERNAL_DATA_COLLECTION -> hAnalytics.allowExternalDataCollection()
    Feature.MOVING_FLOW -> hAnalytics.movingFlow()
    Feature.PAYMENT_SCREEN -> hAnalytics.paymentScreen()
    Feature.QUOTE_CART -> hAnalytics.useQuoteCart()
    Feature.FOREVER -> hAnalytics.forever()
    Feature.REFERRAL_CAMPAIGN -> hAnalytics.foreverFebruaryCampaign()
    Feature.SHOW_BUSINESS_MODEL -> hAnalytics.showCharity()
    Feature.TERMINATION_FLOW -> hAnalytics.terminationFlow()
    Feature.UPDATE_NECESSARY -> hAnalytics.updateNecessary()
    Feature.NEW_MOVING_FLOW -> hAnalytics.movingFlowClient()
    Feature.TRAVEL_CERTIFICATE -> hAnalytics.travelInsurance()
    Feature.EDIT_COINSURED -> hAnalytics.editCoinsured()
    Feature.HELP_CENTER -> false // todo help-center: consider feature flagging if we want to make a release in-between
  }
}
