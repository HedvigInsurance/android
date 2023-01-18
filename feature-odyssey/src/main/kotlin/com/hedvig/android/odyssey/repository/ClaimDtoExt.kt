package com.hedvig.android.odyssey.repository

import com.hedvig.android.odyssey.model.Claim
import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.android.odyssey.model.Input
import com.hedvig.android.odyssey.model.Resolution

fun AutomationClaimDTO2.toClaim() = Claim(
  state = ClaimState(
    dateOfOccurrence = null, // parse localdate
    audioUrl = audioUrl,
    location = location ?: AutomationClaimDTO2.ClaimLocation.AT_HOME,
    item = ClaimState.ItemState(
      purchasePrice = items.firstOrNull()?.purchasePrice,
    ),
  ),
  inputs = inputs.map {
    when (it) {
      is AutomationClaimInputDTO2.AudioRecording -> Input.AudioRecording(
        audioUrl = it.audioUrl,
        file = null,
        questions = it.questions,
      )
      is AutomationClaimInputDTO2.DateOfOccurrence -> Input.DateOfOccurrence(
        selectedDateOfOccurrence = it.dateOfOccurrence,
      )
      is AutomationClaimInputDTO2.Location -> Input.Location(
        selectedLocation = it.location,
        locationOptions = it.options.map { it },
      )
      is AutomationClaimInputDTO2.SingleItem -> Input.SingleItem(
        purchasePrice = it.purchasePrice,
        purchaseDate = it.purchaseDate,
        problemIds = it.options.itemProblems.map { it.problemId },
        selectedProblemIds = it.problemIds,
      )
      AutomationClaimInputDTO2.Unknown -> Input.Unknown
    }
  },
  resolutions = resolutions.map {
    when (it) {
      is AutomationClaimResolutionDTO2.SingleItemPayout -> Resolution.SingleItemPayout(
        payoutAmount = it.payoutAmount,
        purchasePrice = it.purchasePrice,
        depreciation = it.depreciation,
        deductible = it.deductible,
      )
    }
  }.toSet(),
)
