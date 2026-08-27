package com.hedvig.android.feature.onboarding.data

import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId

/**
 * Computes which onboarding steps apply to this member, in presentation order. Pure so the skip
 * rules stay unit-testable. Welcome is not in the result: it is the flow root itself, occupying
 * progress position 0, so the progress bar renders `size + 1` segments.
 *
 * A step left out here is absent from the flow rather than skipped within it, so it also stops
 * taking up a progress-bar segment. That is what [showAnalyticsConsent] relies on.
 */
internal fun buildOnboardingPath(data: OnboardingData, showAnalyticsConsent: Boolean): List<OnboardingStepId> =
  buildList {
    if (showAnalyticsConsent) {
      add(OnboardingStepId.AnalyticsConsent)
    }
    add(OnboardingStepId.PhoneNumber)
    add(OnboardingStepId.Theme)
    if (data.contractsMissingInsuredOrOwnerInfo.isNotEmpty()) {
      add(OnboardingStepId.CoInsured)
    }
    if (data.contractsWithMissingPetId.isNotEmpty()) {
      add(OnboardingStepId.PetIds)
    }
    if (data.referralInformation != null) {
      add(OnboardingStepId.InviteFriend)
    }
    if (!data.hasConnectedPayinMethod) {
      add(OnboardingStepId.ConnectPayment)
    }
    if (data.crossSells.isNotEmpty() && !data.hasOnlyAccidentContracts) {
      add(OnboardingStepId.BundleDiscount)
    }
  }
