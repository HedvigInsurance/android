package com.hedvig.android.featureflags.flags

enum class Feature(
  // Used to easier get a context of what it's for.
  @Suppress("unused") val explanation: String,
) {
  ALWAYS_AVAILABLE_INBOX_AND_NEW_CHAT(
    "Enables inbox icon always available on the Home screen " +
      "and New conversation button inside the inbox",
  ),
  TERMINATION_FLOW("Shows the button which enters the insurance termination flow from the insurance tab"),
  UPDATE_NECESSARY(
    "Defines the lowest supported app version. Should prompt a user to update if it uses an outdated version.",
  ),
  TRAVEL_ADDON("Let members purchase addons"),
  ENABLE_VIDEO_PLAYER_IN_CHAT_MESSAGES(
    "When enabled, it allows the chat to show media in inline video players in the chat messages",
  ),
  ENABLE_CLAIM_HISTORY("Enables claim history"),
  PUPPY_GUIDE(
    "Controls whether the puppy guide is available in the help center. Backed by the disable_puppy_guide kill switch.",
  ),
}
