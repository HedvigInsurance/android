package com.hedvig.android.feature.home.claimdetail.ui

import com.hedvig.android.feature.home.claimdetail.model.ClaimDetailCardUiState
import com.hedvig.android.feature.home.claimdetail.model.ClaimDetailUiState
import com.hedvig.android.feature.home.claimstatus.claimprogress.ClaimProgressUiState
import com.hedvig.android.feature.home.claimstatus.data.PillUiState
import giraffe.type.ClaimStatus
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.Clock

internal fun PillUiState.Companion.previewList(): List<PillUiState> {
  return PillUiState.PillType.values().dropLast(1).map { pillType ->
    PillUiState(pillType.name, pillType)
  }
}

internal fun ClaimProgressUiState.Companion.previewList(): List<ClaimProgressUiState> {
  return ClaimProgressUiState.ClaimProgressType.values().dropLast(1).map { progressType ->
    ClaimProgressUiState(progressType.name, progressType)
  }
}

internal fun ClaimDetailUiState.Companion.previewData(): ClaimDetailUiState {
  return ClaimDetailUiState(
    claimType = "All-risk",
    insuranceType = "Contents Insurance",
    submittedAt = Clock.System.now().minus(30.minutes),
    closedAt = null,
    claimDetailCard = ClaimDetailCardUiState(
      ClaimProgressUiState.previewList(),
      statusParagraph = """
                |Your claim in being reviewed by one of our insurance specialists.
                | We'll get back to you soon with an update.
      """.trimMargin(),
    ),
    signedAudioURL = null,
    claimStatus = ClaimStatus.BEING_HANDLED,
    pillsUiState = listOf(),
  )
}
