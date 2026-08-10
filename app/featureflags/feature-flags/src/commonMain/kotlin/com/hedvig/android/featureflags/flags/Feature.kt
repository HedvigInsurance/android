package com.hedvig.android.featureflags.flags

enum class Feature(
  // Used to easier get a context of what it's for.
  @Suppress("unused") val explanation: String,
) {
  DISABLE_PUPPY_GUIDE(
    "Kill switch for the puppy guide in the help center. When the toggle is on, the puppy guide is hidden.",
  ),
  DISABLE_TERMINATION_REDIRECTION(
    "Kill switch for the termination survey redirection. When the toggle is on, the client requests the " +
      "survey without redirections (sends redirectionEnabled = false), so the moving option keeps its plain " +
      "sub options instead of the redirection interstitial.",
  ),
  DISABLE_RESUMING_ONGOING_SHOP_SESSIONS(
    "Kill switch for the home screen 'Your quotes' section, which lets a member resume an ongoing " +
      "shopping session they started on the web. When the toggle is on, the section is hidden.",
  ),
  ENABLE_CLAIM_INTENT_RESUME(
    "Enables resuming a draft claim: the draft card on the home screen, the draft-claim dialogs, " +
      "and the resumable-aware leave dialog in the claim chat.",
  ),
  ENABLE_NEW_CONVERSATION_FROM_INBOX(
    "Enables inbox icon always available on the Home screen " +
      "and New conversation button inside the inbox",
  ),
  UPDATE_NECESSARY(
    "Defines the lowest supported app version. Should prompt a user to update if it uses an outdated version.",
  ),
}
