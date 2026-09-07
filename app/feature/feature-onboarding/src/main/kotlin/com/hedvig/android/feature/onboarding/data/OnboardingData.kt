package com.hedvig.android.feature.onboarding.data

import com.hedvig.android.data.coinsured.CoInsuredFlowType

/**
 * Everything the onboarding flow needs, fetched eagerly in one round trip before the flow is
 * shown, so the progress bar length is known up-front and no step blocks on network to render.
 */
internal data class OnboardingData(
  val email: String,
  val phoneNumber: String?,
  val contracts: List<OnboardingContract>,
  val referralInformation: OnboardingReferralInformation?,
  val payinStatus: OnboardingPayinStatus,
  val crossSells: List<OnboardingCrossSell>,
) {
  /** Contracts missing co-insured OR co-owner info, i.e. anything the co-insured step should show. */
  val contractsMissingInsuredOrOwnerInfo: List<OnboardingContract> =
    contracts.filter { it.coInsuredFlowType != null }

  val contractsWithMissingPetId: List<OnboardingContract> =
    contracts.filter { it.isMissingPetId }

  val hasOnlyAccidentContracts: Boolean =
    contracts.isNotEmpty() && contracts.all { it.typeOfContract.contains("ACCIDENT") }

  /**
   * Whether the connect-payment step should be skipped. A [OnboardingPayinStatus.Pending] method
   * counts as connected here so we do not re-prompt during the multi-day bank activation wait; the
   * step UI still distinguishes pending from active so it never falsely claims "connected".
   */
  val hasConnectedPayinMethod: Boolean = payinStatus != OnboardingPayinStatus.NeedsSetup
}

/** Mirrors the backend `MemberPaymentConnectionStatus`. */
internal enum class OnboardingPayinStatus {
  Active,
  Pending,
  NeedsSetup,
}

internal data class OnboardingContract(
  val id: String,
  val displayName: String,
  val exposureName: String,
  val typeOfContract: String,
  val missingCoInsuredCount: Int,
  val missingCoOwnersCount: Int,
  val isMissingPetId: Boolean,
  // Total (non-terminating) people plus their first names, used to summarise a contract's row on the
  // co-insured step: the count while info is still missing, the names once the row is complete.
  val coInsuredCount: Int = 0,
  val coInsuredNames: List<String> = emptyList(),
  val coOwnerCount: Int = 0,
  val coOwnerNames: List<String> = emptyList(),
) {
  /**
   * Which edit flow this contract's missing info needs, or null when nothing is missing. Co-owners
   * takes precedence, mirroring the home-screen "needs co-insured info" reminder rule.
   */
  val coInsuredFlowType: CoInsuredFlowType? = when {
    missingCoOwnersCount > 0 -> CoInsuredFlowType.CoOwners
    missingCoInsuredCount > 0 -> CoInsuredFlowType.CoInsured
    else -> null
  }
}

internal data class OnboardingReferralInformation(
  val code: String,
  val monthlyDiscountPerReferralAmount: Double,
  val currencyCode: String,
)

internal data class OnboardingCrossSell(
  val id: String,
  val title: String,
  val description: String,
  val storeUrl: String,
  val pillowImageUrl: String?,
  val buttonTitle: String,
)
