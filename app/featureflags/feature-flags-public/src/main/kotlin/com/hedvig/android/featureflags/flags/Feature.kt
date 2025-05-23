package com.hedvig.android.featureflags.flags

enum class Feature(
  // Used to easier get a context of what it's for.
  @Suppress("unused") val explanation: String,
) {
  @Suppress("ktlint:standard:max-line-length")
  DISABLE_CHAT(
    "This flag determines if the chat feature inside the app should be disabled. This does not disable the ability to navigate to the chat, only that in the chat feature itself, some information should be shown describing that the chat is currently unavailable and they should check back later.",
  ),
  MOVING_FLOW("Lets a user change their address and get a new offer"),
  PAYMENT_SCREEN("Controls whether the payment screen should be accessible from the profile tab"),
  TERMINATION_FLOW("Shows the button which enters the insurance termination flow from the insurance tab"),
  UPDATE_NECESSARY(
    "Defines the lowest supported app version. Should prompt a user to update if it uses an outdated version.",
  ),
  EDIT_COINSURED("Let member edit co insured"),
  HELP_CENTER("Enable the help center screens"),
  TRAVEL_ADDON("Let members purchase addons"),
  ENABLE_ADDONS_REMOVAL_FROM_MOVING_FLOW(
    "Allow members to to exclude specific addons to their home insurance when completing the moving flow",
  ),
  ENABLE_VIDEO_PLAYER_IN_CHAT_MESSAGES(
    "When enabled, it allows the chat to show media in inline video players in the chat messages",
  ),
  DISABLE_REDEEM_CAMPAIGN("Disables the ability to redeem a campaign code"),
}
