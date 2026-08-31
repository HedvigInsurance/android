package com.hedvig.android.feature.onboarding

import app.cash.turbine.Turbine
import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.datastore.FakeGetAnalyticsConsentUseCase
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.feature.onboarding.data.OnboardingContract
import com.hedvig.android.feature.onboarding.data.OnboardingCrossSell
import com.hedvig.android.feature.onboarding.data.OnboardingData
import com.hedvig.android.feature.onboarding.data.OnboardingMemberIdProvider
import com.hedvig.android.feature.onboarding.data.OnboardingPayinStatus
import com.hedvig.android.feature.onboarding.data.OnboardingReferralInformation
import com.hedvig.android.feature.onboarding.data.OnboardingRepository
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
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

/**
 * Builds a store with the feature flags a test rarely cares about already answered, so adding a
 * dependency to [OnboardingSessionStore] does not mean touching every test that needs one.
 */
internal fun testSessionStore(
  repository: OnboardingRepository,
  memberIdProvider: OnboardingMemberIdProvider = FakeOnboardingMemberIdProvider(),
  analyticsDisabled: Boolean = false,
) = OnboardingSessionStore(
  onboardingRepository = repository,
  memberIdProvider = memberIdProvider,
  getAnalyticsConsentUseCase = FakeGetAnalyticsConsentUseCase(
    initialConsent = AnalyticsConsent.NOT_DECIDED.takeIf { !analyticsDisabled },
  ),
)

internal fun testOnboardingData(
  phoneNumber: String? = "070 990 12 32",
  contracts: List<OnboardingContract> = listOf(
    OnboardingContract(
      id = "contract-1",
      displayName = "Home Insurance",
      exposureName = "Bellmansgatan 19A",
      typeOfContract = "SE_APARTMENT_RENT",
      missingCoInsuredCount = 1,
      missingCoOwnersCount = 0,
      isMissingPetId = false,
    ),
  ),
  referralInformation: OnboardingReferralInformation? = OnboardingReferralInformation("CODE", 10.0, "SEK"),
  payinStatus: OnboardingPayinStatus = OnboardingPayinStatus.NeedsSetup,
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
  payinStatus = payinStatus,
  crossSells = crossSells,
)
