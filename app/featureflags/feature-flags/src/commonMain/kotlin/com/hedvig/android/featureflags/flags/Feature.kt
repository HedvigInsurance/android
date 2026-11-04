package com.hedvig.android.featureflags.flags

enum class Feature(
  // Used to easier get a context of what it's for.
  @Suppress("unused") val explanation: String,
) {
  DISABLE_ANALYTICS(
    "Kill switch for the analytics-consent step in onboarding. When the toggle is on, the step is left out " +
      "of the onboarding path entirely, so it never shows and does not take up a progress-bar segment.",
  ),
  DISABLE_ONBOARDING(
    "Kill switch for the post-login onboarding flow. When the toggle is on, onboarding is never shown.",
  ),
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
  HIDE_CANCEL_INSURANCE_FROM_HOME_SCREEN(
    "When the toggle is on, the cancel insurance option is left out of the 'Change insurance' quick action " +
      "on the home screen, so the home screen does not prompt cancellation. The option stays available " +
      "everywhere else, such as the help center quick actions and insurance details.",
  ),
  UPDATE_NECESSARY(
    "Defines the lowest supported app version. Should prompt a user to update if it uses an outdated version.",
  ),
  YEAR_IN_REVIEW(
    "Shows the 'Year in review' button in the home top app bar, which opens the year's highlights as stories.",
  ),
}
