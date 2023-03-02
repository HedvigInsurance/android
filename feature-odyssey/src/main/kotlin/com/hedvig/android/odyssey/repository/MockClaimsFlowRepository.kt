package com.hedvig.android.odyssey.repository

import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.android.odyssey.model.Input
import com.hedvig.android.odyssey.model.Resolution
import com.hedvig.odyssey.remote.money.MonetaryAmount
import kotlinx.coroutines.delay
import java.time.LocalDate

class MockClaimsFlowRepository : ClaimsFlowRepository {

  private val mockSuccessResult = ClaimResult.Success(
    ClaimState(),
    listOf(
      Input.Location(
        listOf(AutomationClaimDTO2.ClaimLocation.AT_HOME, AutomationClaimDTO2.ClaimLocation.ABROAD),
        selectedLocation = AutomationClaimDTO2.ClaimLocation.ABROAD,
      ),
      Input.DateOfOccurrence(LocalDate.now().toString()),
      Input.SingleItem(
        purchasePrice = MonetaryAmount("1000", "SEK"),
        purchaseDate = "date",
        problemIds = listOf(
          AutomationClaimInputDTO2.SingleItem.ClaimProblem.BROKEN,
          AutomationClaimInputDTO2.SingleItem.ClaimProblem.BROKEN_FRONT,
          AutomationClaimInputDTO2.SingleItem.ClaimProblem.MISSING,
        ),
        selectedProblemIds = listOf(),
        modelOptions = listOf(
          AutomationClaimInputDTO2.SingleItem.ItemOptions.ItemModelOption(
            modelId = "1",
            modelName = "iphone",
            modelImageUrl = null,
            typeId = "phone",
            brandId = "apple",
          ),
        ),
        selectedModelOptionId = null,
      ),
      Input.AudioRecording(
        null,
        null,
        listOf(
          AutomationClaimInputDTO2.AudioRecording.AudioRecordingQuestion
            .CLAIM_QUESTION_WHAT_WHO_TOOK_DAMAGE_OR_NEEDS_REPLACEMENT,
        ),
      ),
    ),
    resolution = Resolution.None,
  )

  override suspend fun getOrCreateClaim(commonClaimId: String?): ClaimResult {
    return mockSuccessResult
  }

  override suspend fun updateClaim(claimState: ClaimState, nrOfInputs: Int): ClaimResult {
    delay(1000)
    return mockSuccessResult.copy(
      resolution = Resolution.SingleItemPayout(
        purchasePrice = MonetaryAmount("1000", "SEK"),
        depreciation = MonetaryAmount("1000", "SEK"),
        deductible = MonetaryAmount("1000", "SEK"),
        payoutAmount = MonetaryAmount("1000", "SEK"),
      ),
    )
  }

  override suspend fun getClaim(): ClaimResult {
    delay(1000)
    return mockSuccessResult
  }

  override suspend fun openClaim(amount: MonetaryAmount?) = mockSuccessResult
}
