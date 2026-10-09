package com.hedvig.android.app.navigation

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

/**
 * Locks the analytics names that Datadog metric filters match on.
 *
 * A pinned name is a constant, so it survives the class being renamed or moved. What it does not
 * survive is somebody editing the constant, and that is what this guards: the expected map below is
 * the contract with Datadog, and changing a value here without changing the matching metric filter
 * makes that metric read zero with nothing else to show for it.
 *
 * Implementing `AnalyticsNamed` is how a destination opts in. The map is the net that catches it
 * afterwards, so add the entry in the same change that adds the Datadog metric which needs it, and name
 * that metric in the comment, the way the groups below do.
 */
internal class AnalyticsNameTest {
  @Test
  fun `pinned keys report exactly the names Datadog filters on`() {
    assertThat(PinnedAnalyticsNames.byClassName).isEqualTo(EXPECTED_NAMES)
  }

  private companion object {
    /** Key class name to the name Datadog matches. The two are independent: the class can be renamed or
     * moved while the pinned name stays put, and this map is the contract. */
    val EXPECTED_NAMES = mapOf(
      // android.chat.network.count, android.chat.network.failure
      "com.hedvig.android.feature.chat.navigation.ChatKey" to
        "com.hedvig.android.feature.chat.navigation.ChatKey",
      // android.changeaddress.view.count
      "com.hedvig.android.feature.movingflow.SuccessfulMoveKey" to
        "com.hedvig.android.feature.movingflow.SuccessfulMoveKey",
      // android.claim.started, plus the android.claimflow.* package wildcard
      "com.hedvig.android.feature.claim.chat.navigation.ClaimChatKey" to
        "com.hedvig.android.feature.claim.chat.navigation.ClaimChatKey",
      // android.claim.success, plus the android.claimflow.* package wildcard
      "com.hedvig.android.feature.claim.chat.navigation.ClaimOutcomeNewClaimKey" to
        "com.hedvig.android.feature.claim.chat.navigation.ClaimOutcomeNewClaimKey",
      // android.claimflow.network.count, android.claimflow.network.failure (package wildcard)
      "com.hedvig.android.feature.claim.chat.navigation.ClaimOutcomeDeflectKey" to
        "com.hedvig.android.feature.claim.chat.navigation.ClaimOutcomeDeflectKey",
      "com.hedvig.android.feature.claim.chat.navigation.UpdateAppKey" to
        "com.hedvig.android.feature.claim.chat.navigation.UpdateAppKey",
      "com.hedvig.android.feature.claim.chat.navigation.StartClaimPledgeKey" to
        "com.hedvig.android.feature.claim.chat.navigation.StartClaimPledgeKey",
      // android.terminateinsurance.network.count, android.terminateinsurance.network.error
      // (package wildcard)
      "com.hedvig.android.feature.terminateinsurance.navigation.TerminationSurveyFirstStepKey" to
        "com.hedvig.android.feature.terminateinsurance.navigation.TerminationSurveyFirstStepKey",
      "com.hedvig.android.feature.terminateinsurance.navigation.TerminationSurveySecondStepKey" to
        "com.hedvig.android.feature.terminateinsurance.navigation.TerminationSurveySecondStepKey",
      "com.hedvig.android.feature.terminateinsurance.navigation.TerminationRedirectionKey" to
        "com.hedvig.android.feature.terminateinsurance.navigation.TerminationRedirectionKey",
      "com.hedvig.android.feature.terminateinsurance.navigation.TerminationDateKey" to
        "com.hedvig.android.feature.terminateinsurance.navigation.TerminationDateKey",
      "com.hedvig.android.feature.terminateinsurance.navigation.TerminationConfirmationKey" to
        "com.hedvig.android.feature.terminateinsurance.navigation.TerminationConfirmationKey",
      "com.hedvig.android.feature.terminateinsurance.navigation.InsuranceDeletionKey" to
        "com.hedvig.android.feature.terminateinsurance.navigation.InsuranceDeletionKey",
      "com.hedvig.android.feature.terminateinsurance.navigation.TerminationSuccessKey" to
        "com.hedvig.android.feature.terminateinsurance.navigation.TerminationSuccessKey",
      "com.hedvig.android.feature.terminateinsurance.navigation.TerminationFailureKey" to
        "com.hedvig.android.feature.terminateinsurance.navigation.TerminationFailureKey",
      "com.hedvig.android.feature.terminateinsurance.navigation.UnknownScreenKey" to
        "com.hedvig.android.feature.terminateinsurance.navigation.UnknownScreenKey",
      "com.hedvig.android.feature.terminateinsurance.navigation.DeflectSuggestionKey" to
        "com.hedvig.android.feature.terminateinsurance.navigation.DeflectSuggestionKey",
    )
  }
}
