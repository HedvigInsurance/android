package com.hedvig.android.odyssey.model

import com.hedvig.android.odyssey.repository.AutomationClaimDTO2
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import com.hedvig.common.remote.file.File
import com.hedvig.common.remote.money.MonetaryAmount

/**
 * An Input describes what the user needs to update the claim with in order to continue the automation flow.
 * Map the input fields to the corresponding fields on ClaimState when updating the claim.
 */
sealed interface Input {
  data class AudioRecording(
    val audioUrl: String?,
    val file: File? = null,
    val questions: List<AutomationClaimInputDTO2.AudioRecording.AudioRecordingQuestion>,
  ) : Input

  data class DateOfOccurrence(
    val selectedDateOfOccurrence: String?,
  ) : Input

  data class Location(
    val locationOptions: List<AutomationClaimDTO2.ClaimLocation>,
    val selectedLocation: AutomationClaimDTO2.ClaimLocation?,
  ) : Input

  data class DateOfOccurrencePlusLocation(
    val selectedDateOfOccurrence: String?,
    val locationOptions: List<AutomationClaimDTO2.ClaimLocation>,
    val selectedLocation: AutomationClaimDTO2.ClaimLocation?,
  ) : Input

  data class SingleItem(
    val purchasePrice: MonetaryAmount?,
    val purchaseDate: String?,
    val problemIds: List<AutomationClaimInputDTO2.SingleItem.ClaimProblem>,
    val selectedProblemIds: List<AutomationClaimInputDTO2.SingleItem.ClaimProblem>,
    val modelOptions: List<AutomationClaimInputDTO2.SingleItem.ItemOptions.ItemModelOption>,
    val selectedModelOptionId: String?,
  ) : Input

  data class PhoneNumber(
    val phoneNumber: String,
  ) : Input

  object Unknown : Input
}
