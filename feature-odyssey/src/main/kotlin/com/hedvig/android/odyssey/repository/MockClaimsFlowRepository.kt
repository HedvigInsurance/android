package com.hedvig.android.odyssey.repository

import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.android.odyssey.model.Input
import com.hedvig.android.odyssey.model.Resolution
import com.hedvig.odyssey.remote.money.MonetaryAmount
import java.time.LocalDate
import kotlinx.coroutines.delay


class MockClaimsFlowRepository : ClaimsFlowRepository {

  override suspend fun getOrCreateClaim(commonClaimId: String?): ClaimResult {
    return ClaimResult.Success(
      ClaimState(),
      listOf(
        Input.Location(
          listOf(AutomationClaimDTO2.ClaimLocation.AT_HOME, AutomationClaimDTO2.ClaimLocation.ABROAD),
          selectedLocation = AutomationClaimDTO2.ClaimLocation.ABROAD,
        ),
        Input.DateOfOccurrence(LocalDate.now().toString()),
        Input.AudioRecording(
          null,
          null,
          listOf(AutomationClaimInputDTO2.AudioRecording.AudioRecordingQuestion.CLAIM_QUESTION_WHAT_WHO_TOOK_DAMAGE_OR_NEEDS_REPLACEMENT),
        ),
      ),
      resolution = Resolution.None,
    )
  }

  override suspend fun updateClaim(claimState: ClaimState, nrOfInputs: Int): ClaimResult {
    delay(1000)
    return ClaimResult.Success(
      claimState = ClaimState(),
      inputs = listOf(
        Input.Location(
          listOf(AutomationClaimDTO2.ClaimLocation.AT_HOME, AutomationClaimDTO2.ClaimLocation.ABROAD),
          selectedLocation = AutomationClaimDTO2.ClaimLocation.ABROAD,
        ),
        Input.DateOfOccurrence(LocalDate.now().toString()),
        Input.AudioRecording(
          null,
          null,
          listOf(AutomationClaimInputDTO2.AudioRecording.AudioRecordingQuestion.CLAIM_QUESTION_WHAT_WHO_TOOK_DAMAGE_OR_NEEDS_REPLACEMENT),
        ),
      ),
      resolution = Resolution.SingleItemPayout(
        purchasePrice = MonetaryAmount("2000", "SEK"),
        depreciation = MonetaryAmount("2000", "SEK"),
        deductible = MonetaryAmount("2000", "SEK"),
        payoutAmount = MonetaryAmount("2000", "SEK"),
      ),
    )
  }

  override suspend fun getClaim(): ClaimResult {
    delay(1000)
    return ClaimResult.Success(
      ClaimState(),
      listOf(
        Input.AudioRecording(
          null,
          null,
          listOf(),
        ),
      ),
      resolution = Resolution.None,
    )
  }

  override suspend fun openClaim(amount: MonetaryAmount?) = ClaimResult.Success(
    claimState = ClaimState(),
    inputs = listOf(
      Input.Location(
        listOf(AutomationClaimDTO2.ClaimLocation.AT_HOME, AutomationClaimDTO2.ClaimLocation.ABROAD),
        selectedLocation = AutomationClaimDTO2.ClaimLocation.ABROAD,
      ),
      Input.DateOfOccurrence(LocalDate.now().toString()),
      Input.AudioRecording(
        null,
        null,
        listOf(AutomationClaimInputDTO2.AudioRecording.AudioRecordingQuestion.CLAIM_QUESTION_WHAT_WHO_TOOK_DAMAGE_OR_NEEDS_REPLACEMENT),
      ),
    ),
    resolution = Resolution.SingleItemPayout(
      purchasePrice = MonetaryAmount("2000", "SEK"),
      depreciation = MonetaryAmount("2000", "SEK"),
      deductible = MonetaryAmount("2000", "SEK"),
      payoutAmount = MonetaryAmount("2000", "SEK"),
    ),
  )
}
