package com.hedvig.android.feature.onboarding.data

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.doesNotContain
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.hedvig.android.feature.onboarding.data.OnboardingPayinStatus
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import org.junit.Test

class OnboardingPathTest {
  private fun contract(
    id: String = "contract-1",
    typeOfContract: String = "SE_APARTMENT_RENT",
    missingCoInsuredCount: Int = 0,
    missingCoOwnersCount: Int = 0,
    isMissingPetId: Boolean = false,
  ) = OnboardingContract(
    id = id,
    displayName = "Home Insurance",
    exposureName = "Bellmansgatan 19A",
    typeOfContract = typeOfContract,
    missingCoInsuredCount = missingCoInsuredCount,
    missingCoOwnersCount = missingCoOwnersCount,
    isMissingPetId = isMissingPetId,
  )

  private fun data(
    contracts: List<OnboardingContract> = listOf(contract()),
    referralInformation: OnboardingReferralInformation? = OnboardingReferralInformation("CODE", 10.0, "SEK"),
    hasConnectedPayinMethod: Boolean = false,
    crossSells: List<OnboardingCrossSell> = listOf(
      OnboardingCrossSell(
        id = "cs",
        title = "Pet",
        description = "For your pet",
        storeUrl = "https://x",
        pillowImageUrl = "https://x",
        buttonTitle = "See price",
      ),
    ),
  ) = OnboardingData(
    email = "member@example.com",
    phoneNumber = "070 990 12 32",
    contracts = contracts,
    referralInformation = referralInformation,
    payinStatus = if (hasConnectedPayinMethod) OnboardingPayinStatus.Active else OnboardingPayinStatus.NeedsSetup,
    crossSells = crossSells,
  )

  private fun path(data: OnboardingData = data(), showAnalyticsConsent: Boolean = true) =
    buildOnboardingPath(data, showAnalyticsConsent)

  @Test
  fun `consent, phone and theme are always present, in order, at the front`() {
    val path = path(data())
    assertThat(path.take(3)).containsExactly(
      OnboardingStepId.AnalyticsConsent,
      OnboardingStepId.PhoneNumber,
      OnboardingStepId.Theme,
    )
  }

  @Test
  fun `analytics consent step is left out entirely when analytics is disabled`() {
    val path = path(showAnalyticsConsent = false)
    assertThat(path).doesNotContain(OnboardingStepId.AnalyticsConsent)
    assertThat(path.first()).isEqualTo(OnboardingStepId.PhoneNumber)
  }

  @Test
  fun `disabling analytics shortens the path by exactly one step`() {
    assertThat(path(showAnalyticsConsent = false)).hasSize(path().size - 1)
  }

  @Test
  fun `everything applicable produces the full path in canonical order`() {
    val path = path(
      data(contracts = listOf(contract(missingCoInsuredCount = 1, isMissingPetId = true))),
    )
    assertThat(path).containsExactly(
      OnboardingStepId.AnalyticsConsent,
      OnboardingStepId.PhoneNumber,
      OnboardingStepId.Theme,
      OnboardingStepId.CoInsured,
      OnboardingStepId.PetIds,
      OnboardingStepId.InviteFriend,
      OnboardingStepId.ConnectPayment,
      OnboardingStepId.BundleDiscount,
    )
  }

  @Test
  fun `co-insured step is included when a contract is missing co-owner info`() {
    val path = path(data(contracts = listOf(contract(missingCoOwnersCount = 1))))
    assertThat(path).contains(OnboardingStepId.CoInsured)
  }

  @Test
  fun `co-insured step is skipped when no contract has missing co-insured info`() {
    val path = path(data(contracts = listOf(contract(missingCoInsuredCount = 0))))
    assertThat(path).doesNotContain(OnboardingStepId.CoInsured)
  }

  @Test
  fun `pet id step is skipped when no contract is missing a pet id`() {
    val path = path(data(contracts = listOf(contract(isMissingPetId = false))))
    assertThat(path).doesNotContain(OnboardingStepId.PetIds)
  }

  @Test
  fun `invite step is skipped without referral information`() {
    val path = path(data(referralInformation = null))
    assertThat(path).doesNotContain(OnboardingStepId.InviteFriend)
  }

  @Test
  fun `connect payment step is skipped when a payin method is already connected`() {
    val path = path(data(hasConnectedPayinMethod = true))
    assertThat(path).doesNotContain(OnboardingStepId.ConnectPayment)
  }

  @Test
  fun `bundle step is skipped when there are no cross sells`() {
    val path = path(data(crossSells = emptyList()))
    assertThat(path).doesNotContain(OnboardingStepId.BundleDiscount)
  }

  @Test
  fun `bundle step is skipped for accident-only members even with cross sells`() {
    val path = path(data(contracts = listOf(contract(typeOfContract = "SE_ACCIDENT"))))
    assertThat(path).doesNotContain(OnboardingStepId.BundleDiscount)
  }

  @Test
  fun `bundle step is present for a mixed portfolio that includes accident`() {
    val path = path(
      data(contracts = listOf(contract(typeOfContract = "SE_ACCIDENT"), contract(id = "c2"))),
    )
    assertThat(path).contains(OnboardingStepId.BundleDiscount)
  }

  @Test
  fun `a member with nothing applicable gets exactly the mandatory steps`() {
    val path = path(
      data(
        contracts = listOf(contract()),
        referralInformation = null,
        hasConnectedPayinMethod = true,
        crossSells = emptyList(),
      ),
    )
    assertThat(path).containsExactly(
      OnboardingStepId.AnalyticsConsent,
      OnboardingStepId.PhoneNumber,
      OnboardingStepId.Theme,
    )
  }
}
