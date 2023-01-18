package com.hedvig.android.odyssey.model

import com.hedvig.android.odyssey.repository.AutomationClaimDTO2
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import com.hedvig.common.remote.file.File
import com.hedvig.common.remote.money.MonetaryAmount

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
    val selectedDateOfOccurrence: String?,
  ) : Input

  data class Location(
    val locationOptions: List<AutomationClaimDTO2.ClaimLocation>,
    val selectedLocation: AutomationClaimDTO2.ClaimLocation?,
  ) : Input

  data class SingleItem(
    val purchasePrice: MonetaryAmount?,
    val purchaseDate: String?,
    val problemIds: List<AutomationClaimInputDTO2.SingleItem.ClaimProblem>,
    val selectedProblemIds: List<AutomationClaimInputDTO2.SingleItem.ClaimProblem>,
  ) : Input

  data class PhoneNumber(
    val phoneNumber: String,
  ) : Input

  object Unknown : Input
}
