package com.hedvig.android.feature.home.claimdetail.model

import com.hedvig.android.audio.player.SignedAudioUrl
import giraffe.ClaimDetailsQuery
import giraffe.type.ClaimStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant

internal data class ClaimDetailUiState(
  val claimType: String,
  val insuranceType: String,
  val claimDetailResult: ClaimDetailResult,
  val submittedAt: Instant,
  val closedAt: Instant?,
  val claimDetailCard: ClaimDetailCardUiState,
  val signedAudioURL: SignedAudioUrl?,
  val claimStatus: ClaimStatus,
) {
  companion object {
    fun fromDto(dto: ClaimDetailsQuery.ClaimDetail): ClaimDetailUiState {
      return ClaimDetailUiState(
        claimType = dto.title,
        insuranceType = dto.subtitle,
        claimDetailResult = ClaimDetailResult.fromDto(dto),
        submittedAt = dto.claim.submittedAt.toKotlinInstant(),
        closedAt = dto.claim.closedAt?.toKotlinInstant(),
        claimDetailCard = ClaimDetailCardUiState.fromDto(dto),
        signedAudioURL = SignedAudioUrl.fromSignedAudioUrlStringOrNull(dto.claim.signedAudioURL),
        claimStatus = dto.claim.status,
      )
    }
  }
}
