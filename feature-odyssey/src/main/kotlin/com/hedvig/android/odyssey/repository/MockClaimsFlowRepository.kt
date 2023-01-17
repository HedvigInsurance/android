package com.hedvig.android.odyssey.repository

import com.hedvig.android.odyssey.model.Claim
import com.hedvig.android.odyssey.ClaimResult
import com.hedvig.android.odyssey.ClaimsFlowRepository
import com.hedvig.android.odyssey.model.Input
import com.hedvig.android.odyssey.model.Resolution
import com.hedvig.common.remote.money.MonetaryAmount
import java.time.LocalDate
import kotlinx.coroutines.delay


class MockClaimsFlowRepository : ClaimsFlowRepository {

  override suspend fun createOrRestartClaim(itemType: String?, itemProblem: String?): ClaimResult {
    return ClaimResult.Success(
      Claim(
        Claim.ClaimState(),
        listOf(
          Input.Location(listOf(AutomationClaimDTO2.ClaimLocation.AT_HOME, AutomationClaimDTO2.ClaimLocation.ABROAD)),
          Input.DateOfOccurrence(LocalDate.now()),
          Input.AudioRecording(
            null,
            null,
            listOf("Q1", "Q2"),
          ),
        ),
      ),
    )
  }

  override suspend fun updateClaim(claimState: Claim.ClaimState): ClaimResult {
    delay(1000)
    return ClaimResult.Success(
      Claim(
        state = Claim.ClaimState(),
        inputs = listOf(
          Input.Location(listOf(AutomationClaimDTO2.ClaimLocation.AT_HOME, AutomationClaimDTO2.ClaimLocation.ABROAD)),
          Input.DateOfOccurrence(LocalDate.now()),
          Input.AudioRecording(
            null,
            null,
            listOf("Q1", "Q2"),
          ),
        ),
        resolutions = listOf(
          Resolution.SingleItemPayout(
            purchasePrice = MonetaryAmount("2000", "SEK"),
            depreciation = MonetaryAmount("2000", "SEK"),
            deductible = MonetaryAmount("2000", "SEK"),
            payoutAmount = MonetaryAmount("2000", "SEK"),
          ),
        ).toSet(),
      ),
    )
  }

  override suspend fun getClaim(): ClaimResult {
    delay(1000)
    return ClaimResult.Success(
      Claim(
        Claim.ClaimState(),
        listOf(
          Input.AudioRecording(
            null,
            null,
            listOf(),
          ),
        ),
      ),
    )
  }

  override suspend fun openClaim(amount: MonetaryAmount?) = ClaimResult.Success(
    Claim(
      state = Claim.ClaimState(),
      inputs = listOf(
        Input.Location(listOf(AutomationClaimDTO2.ClaimLocation.AT_HOME, AutomationClaimDTO2.ClaimLocation.ABROAD)),
        Input.DateOfOccurrence(LocalDate.now()),
        Input.AudioRecording(
          null,
          null,
          listOf("Q1", "Q2"),
        ),
      ),
      resolutions = listOf(
        Resolution.SingleItemPayout(
          purchasePrice = MonetaryAmount("2000", "SEK"),
          depreciation = MonetaryAmount("2000", "SEK"),
          deductible = MonetaryAmount("2000", "SEK"),
          payoutAmount = MonetaryAmount("2000", "SEK"),
        ),
      ).toSet(),
    ),
  )
}
