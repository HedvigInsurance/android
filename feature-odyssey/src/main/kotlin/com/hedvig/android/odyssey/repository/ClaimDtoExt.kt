package com.hedvig.android.odyssey.repository

import com.hedvig.android.odyssey.model.Claim
import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.android.odyssey.model.Input
import com.hedvig.android.odyssey.model.Resolution

fun AutomationClaimDTO2.toClaim(nrOfInputs: Int?) = Claim(
  state = ClaimState(
    dateOfOccurrence = null, // parse localdate
    audioUrl = audioUrl,
    location = location ?: AutomationClaimDTO2.ClaimLocation.AT_HOME,
    item = ClaimState.ItemState(
      purchasePrice = items.firstOrNull()?.purchasePrice,
    ),
  ),
  inputs = getInputs(),
  resolution = getResolution(nrOfInputs),
)

private fun AutomationClaimDTO2.getInputs(): List<Input> {
  val claimInputs = mutableListOf<Input>()
  val audioRecording = inputs.filterIsInstance<AutomationClaimInputDTO2.AudioRecording>().firstOrNull()
  if (audioRecording != null) {
    claimInputs.add(
      Input.AudioRecording(
        audioUrl = audioRecording.audioUrl,
        file = null,
        questions = audioRecording.questions,
      ),
    )
  }

  val dateOfOccurrence = inputs.filterIsInstance<AutomationClaimInputDTO2.DateOfOccurrence>().firstOrNull()
  val location = inputs.filterIsInstance<AutomationClaimInputDTO2.Location>().firstOrNull()

  if (dateOfOccurrence != null && location != null) {
    claimInputs.add(
      Input.DateOfOccurrencePlusLocation(
        selectedDateOfOccurrence = dateOfOccurrence.dateOfOccurrence,
        locationOptions = location.options,
        selectedLocation = location.location,
      ),
    )
  }

  if (dateOfOccurrence != null && location == null) {
    claimInputs.add(
      Input.DateOfOccurrence(
        selectedDateOfOccurrence = dateOfOccurrence.dateOfOccurrence,
      ),
    )
  }

  if (location != null && dateOfOccurrence == null) {
    claimInputs.add(
      Input.Location(
        selectedLocation = location.location,
        locationOptions = location.options.map { it },
      ),
    )
  }

  val singleItem = inputs.filterIsInstance<AutomationClaimInputDTO2.SingleItem>().firstOrNull()
  if (singleItem != null) {
    claimInputs.add(
      Input.SingleItem(
        purchasePrice = singleItem.purchasePrice,
        purchaseDate = singleItem.purchaseDate,
        problemIds = singleItem.options.itemProblems.map { it.problemId },
        selectedProblemIds = singleItem.problemIds,
      ),
    )
  }
  return claimInputs.toList()
}

private fun AutomationClaimDTO2.getResolution(nrOfInputs: Int?): Resolution {

  var resolutions: Set<Resolution> = resolutions.map {
    when (it) {
      is AutomationClaimResolutionDTO2.SingleItemPayout -> Resolution.SingleItemPayout(
        payoutAmount = it.payoutAmount,
        purchasePrice = it.purchasePrice,
        depreciation = it.depreciation,
        deductible = it.deductible,
      )
    }
  }.toSet()

  if (nrOfInputs != null && getInputs().size == nrOfInputs) {
    resolutions = resolutions.plus(Resolution.ManualHandling)
  }

  return resolutions.firstOrNull() ?: Resolution.None
}
