package com.hedvig.android.feature.onboarding.data

/**
 * Everything the onboarding flow needs, fetched eagerly in one round trip before the flow is
 * shown, so the progress bar length is known up-front and no step blocks on network to render.
 */
internal data class OnboardingData(
  val email: String,
  val phoneNumber: String?,
  val contracts: List<OnboardingContract>,
  val referralInformation: OnboardingReferralInformation?,
  val hasConnectedPayinMethod: Boolean,
  val crossSells: List<OnboardingCrossSell>,
) {
  val contractsWithMissingCoInsured: List<OnboardingContract> =
    contracts.filter { it.missingCoInsuredCount > 0 }

  val contractsWithMissingPetId: List<OnboardingContract> =
    contracts.filter { it.isMissingPetId }

  val hasOnlyAccidentContracts: Boolean =
    contracts.isNotEmpty() && contracts.all { it.typeOfContract.contains("ACCIDENT") }
}

internal data class OnboardingContract(
  val id: String,
  val displayName: String,
  val exposureName: String,
  val typeOfContract: String,
  val missingCoInsuredCount: Int,
  val isMissingPetId: Boolean,
)

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
)
