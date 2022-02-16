package com.hedvig.app.feature.claimdetail.model

import com.hedvig.android.owldroid.graphql.ClaimDetailsQuery
import com.hedvig.android.owldroid.type.ClaimStatus
import java.time.Instant

data class ClaimDetailUiState(
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
                submittedAt = dto.claim.submittedAt,
                closedAt = dto.claim.closedAt,
                claimDetailCard = ClaimDetailCardUiState.fromDto(dto),
                signedAudioURL = SignedAudioUrl.fromSignedAudioUrlStringOrNull(dto.claim.signedAudioURL),
                claimStatus = dto.claim.status,
            )
        }
    }
}
