package com.hedvig.android.feature.onboarding.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

/**
 * The onboarding flow root, rendering the welcome step. Pushed on top of Home by the onboarding
 * gate in `:app` for members who have not seen onboarding yet.
 */
@androidx.annotation.Keep
@Serializable
data object OnboardingKey : HedvigNavKey

/**
 * One entry per onboarding step after welcome. Deliberately tiny: the eagerly-fetched flow data
 * lives in the ActivityRetainedScope OnboardingSessionStore, not in the key, so back-stack
 * persistence stays cheap and process-death restore re-fetches instead of deserializing a blob.
 */
@androidx.annotation.Keep
@Serializable
internal data class OnboardingStepKey(
  val stepId: OnboardingStepId,
) : HedvigNavKey

/**
 * Hosts the shared Forever screen inside the onboarding flow, so the member can invite friends
 * without leaving onboarding. Reached from the invite step's "Invite a friend" button and left with
 * system back, which pops back to the invite step still sitting in the back stack.
 */
@androidx.annotation.Keep
@Serializable
internal data object OnboardingForeverKey : HedvigNavKey

/**
 * Identifies each onboarding step past welcome. The member's concrete path (which of these apply,
 * in order) is computed by buildOnboardingPath from the eagerly fetched OnboardingData.
 */
enum class OnboardingStepId {
  AnalyticsConsent,
  PhoneNumber,
  Theme,
  CoInsured,
  PetIds,
  InviteFriend,
  ConnectPayment,
  BundleDiscount,
}
