package com.hedvig.android.odyssey.repository

import com.hedvig.android.odyssey.Claim
import com.hedvig.android.odyssey.Input
import com.hedvig.android.odyssey.Resolution

fun AutomationClaimDTO2.toClaim() = Claim(
  state = Claim.ClaimState(
    dateOfOccurrence = null,
    audioUrl = audioUrl,
    location = location ?: AutomationClaimDTO2.ClaimLocation.AT_HOME,
  ),
  inputs = inputs.map {
    when (it) {
      is AutomationClaimInputDTO2.AudioRecording -> Input.AudioRecording(
        audioUrl = it.audioUrl,
        file = null,
        questions = it.questions,
      )
      is AutomationClaimInputDTO2.DateOfOccurrence -> Input.DateOfOccurrence(
        dateOfOccurrence = null,
      )
      is AutomationClaimInputDTO2.Location -> Input.Location(
        locationOptions = it.options.map { it },
      )
      is AutomationClaimInputDTO2.SingleItem -> Input.SingleItem(
        purchasePrice = it.purchasePrice,
        problemIds = it.options.itemProblems.map { it.problemId },
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
