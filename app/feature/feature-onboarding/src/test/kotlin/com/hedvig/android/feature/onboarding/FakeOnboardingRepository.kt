package com.hedvig.android.feature.onboarding

import app.cash.turbine.Turbine
import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.onboarding.data.OnboardingContract
import com.hedvig.android.feature.onboarding.data.OnboardingCrossSell
import com.hedvig.android.feature.onboarding.data.OnboardingData
import com.hedvig.android.feature.onboarding.data.OnboardingMemberIdProvider
import com.hedvig.android.feature.onboarding.data.OnboardingReferralInformation
import com.hedvig.android.feature.onboarding.data.OnboardingRepository
import kotlinx.coroutines.flow.flowOf

internal class FakeOnboardingMemberIdProvider(var memberId: String? = "test-member-id") : OnboardingMemberIdProvider {
  override fun memberId() = flowOf(memberId)
}

internal class FakeOnboardingRepository : OnboardingRepository {
  val onboardingDataResponses = Turbine<Either<ErrorMessage, OnboardingData>>()
  val updateContactInfoResponses = Turbine<Either<ErrorMessage, Unit>>()
  var updateContactInfoCallCount: Int = 0

  override suspend fun getOnboardingData(): Either<ErrorMessage, OnboardingData> {
    return onboardingDataResponses.awaitItem()
  }

  override suspend fun updateContactInfo(email: String, phoneNumber: String): Either<ErrorMessage, Unit> {
    updateContactInfoCallCount++
    return updateContactInfoResponses.awaitItem()
  }
}

internal fun testOnboardingData(
  phoneNumber: String? = "070 990 12 32",
  contracts: List<OnboardingContract> = listOf(
    OnboardingContract(
      id = "contract-1",
      displayName = "Home Insurance",
      exposureName = "Bellmansgatan 19A",
      typeOfContract = "SE_APARTMENT_RENT",
      missingCoInsuredCount = 1,
      isMissingPetId = false,
    ),
  ),
  referralInformation: OnboardingReferralInformation? = OnboardingReferralInformation("CODE", 10.0, "SEK"),
  hasConnectedPayinMethod: Boolean = false,
  crossSells: List<OnboardingCrossSell> = listOf(
    OnboardingCrossSell(
      id = "cs",
      title = "Pet",
      description = "For your pet",
      storeUrl = "https://x",
      pillowImageUrl = "https://x",
    ),
  ),
): OnboardingData = OnboardingData(
  email = "member@example.com",
  phoneNumber = phoneNumber,
  contracts = contracts,
  referralInformation = referralInformation,
  hasConnectedPayinMethod = hasConnectedPayinMethod,
  crossSells = crossSells,
)
