package com.hedvig.android.odyssey

import com.hedvig.android.odyssey.repository.AutomationClaimDTO2
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import com.hedvig.android.odyssey.repository.AutomationClaimResolutionPayoutMethodDTO2
import com.hedvig.common.remote.file.File
import com.hedvig.common.remote.money.MonetaryAmount
import java.time.LocalDate
import java.util.UUID

data class ViewState(
  val title: String = "Submit Claim",
  val id: UUID = UUID.randomUUID(),
  val claim: Claim? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = false,
  val isLoadingPayment: Boolean = false,
  val isLastScreen: Boolean = false,
  val shouldExit: Boolean = false,
  val resolution: Resolution = Resolution.None,
)

sealed interface Input {

  data class AudioRecording(
    val audioUrl: String?,
    val file: File? = null,
    val questions: List<String?>,
  ) : Input {
    enum class AudioRecordingQuestion {
      CLAIM_QUESTION_WHAT_HAS_HAPPENED,
      CLAIM_QUESTION_WHERE_AND_WHEN_DID_IT_HAPPEN,
      CLAIM_QUESTION_WHERE_DID_IT_HAPPEN,
      CLAIM_QUESTION_WHAT_WHO_TOOK_DAMAGE_OR_NEEDS_REPLACEMENT,
      ;
    }
  }

  data class DateOfOccurrence(
    val dateOfOccurrence: LocalDate?,
  ) : Input

  data class Location(
    val locationOptions: List<AutomationClaimDTO2.ClaimLocation>,
  ) : Input

  data class SingleItem(
    val purchasePrice: MonetaryAmount?,
    val problemIds: List<AutomationClaimInputDTO2.SingleItem.ClaimProblem>,
  ) : Input

  data class PhoneNumber(
    val phoneNumber: String,
  ) : Input

  object Unknown : Input
}

sealed interface Resolution {
  object None : Resolution
  object ManualHandling : Resolution
  data class SingleItemPayout(
    val purchasePrice: MonetaryAmount,
    val depreciation: MonetaryAmount,
    val deductible: MonetaryAmount,
    val payoutAmount: MonetaryAmount,
  ) : Resolution
}
