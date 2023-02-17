package com.hedvig.android.hanalytics.featureflags.flags

enum class Feature(
  @Suppress("unused") val explanation: String, // Used to easier get a context of what it's for.
) {
  CONNECT_PAYMENT_POST_ONBOARDING(
    "Connecting payment post onboarding. Having this OFF means that it must happen in the offer page",
  ),
  EXTERNAL_DATA_COLLECTION("Enables external data collection for offers, from eg. Insurely"),
  FRANCE_MARKET("Used to select french market in app"),
  MOVING_FLOW("Lets a user change their address and get a new offer"),
  QUOTE_CART("Use new APIs for onboarding"),
  REFERRALS("Whether the referrals feature is enabled. Shows/Hides the forever tab. Used for Qasa."),
  REFERRAL_CAMPAIGN("Used to show banner in referral view"),
  SHOW_BUSINESS_MODEL("Show anything related to the the business model or hide it all completely. Used for Qasa."),
  UPDATE_NECESSARY(
    "Defines the lowest supported app version. Should prompt a user to update if it uses an outdated version.",
  ),
  PAYMENT_SCREEN("Controls whether the payment screen should be accessible from the profile tab"),
  CONNECT_PAYIN_REMINDER("Show a reminder to connect payin on the home tab"),
  COMMON_CLAIMS("Show common claims on the home tab"),
  USE_ODYSSEY_CLAIM_FLOW("Whether to show the odyssey claim flow or not"),
  USE_NATIVE_CLAIMS_FLOW("Whether to use the native claims flow")
}
