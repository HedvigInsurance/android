package com.hedvig.android.hanalytics.featureflags.flags

enum class Feature(
  @Suppress("unused") val explanation: String, // Used to easier get a context of what it's for.
) {
  COMMON_CLAIMS("Show common claims on the home tab"),
  CONNECT_PAYIN_REMINDER("Show a reminder to connect payin on the home tab"),
  CONNECT_PAYMENT_POST_ONBOARDING(
    "Connecting payment post onboarding. Having this OFF means that it must happen in the offer page",
  ),
  DISABLE_CHAT(
    "This flag determines if the chat feature inside the app should be disabled. This does not disable the ability to navigate to the chat, only that in the chat feature itself, some information should be shown describing that the chat is currently unavailable and they should check back later.",
  ),
  DISABLE_DARK_MODE(
    "When this flag is on, dark mode is not allowed in the app at all. This also removes the ability to change the theme in the settings completely",
  ),
  EXTERNAL_DATA_COLLECTION("Enables external data collection for offers, from eg. Insurely"),
  MOVING_FLOW("Lets a user change their address and get a new offer"),
  PAYMENT_SCREEN("Controls whether the payment screen should be accessible from the profile tab"),
  QUOTE_CART("Use new APIs for onboarding"),
  FOREVER("Whether the referrals feature is enabled. Shows/Hides the forever tab. Used for Qasa."),
  REFERRAL_CAMPAIGN("Used to show banner in referral view"),
  SHOW_BUSINESS_MODEL("Show anything related to the the business model or hide it all completely. Used for Qasa."),
  TERMINATION_FLOW("Shows the button which enters the insurance termination flow from the insurance tab"),
  UPDATE_NECESSARY(
    "Defines the lowest supported app version. Should prompt a user to update if it uses an outdated version.",
  ),
  USE_NATIVE_CLAIMS_FLOW("Whether to use the native odyssey claims flow, with odyssey backend"),
  NEW_MOVING_FLOW("Moving flow from octopus. New design."),
  CLAIMS_TRIAGING("Select claim group before starting claims flow."),
  TRAVEL_CERTIFICATE("Let member generate travel certificate in app."),
}
