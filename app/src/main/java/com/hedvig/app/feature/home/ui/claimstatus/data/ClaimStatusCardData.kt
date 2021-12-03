package com.hedvig.app.feature.home.ui.claimstatus.data

import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.feature.claimdetail.ClaimDetailParameter

data class ClaimStatusCardData(
    val id: String,
    val pillData: List<PillData>,
    val title: String,
    val subtitle: String,
    val claimProgressData: List<ClaimProgressData>,
    val detailParameter: ClaimDetailParameter,
) {
    companion object {
        fun fromClaimStatusCardsQuery(
            claimStatusCard: HomeQuery.ClaimStatusCard,
        ): ClaimStatusCardData {
            val claimProgressData = ClaimProgressData.fromClaimStatusCardsQuery(claimStatusCard)
            return ClaimStatusCardData(
                id = claimStatusCard.id,
                pillData = PillData.fromClaimStatusCardsQuery(claimStatusCard),
                title = claimStatusCard.title,
                subtitle = claimStatusCard.subtitle,
                claimProgressData = claimProgressData,
                detailParameter = ClaimDetailParameter(
                    claimType = claimStatusCard.claim.type ?: "", // Switch this to non-null string?
                    submittedAt = claimStatusCard.claim.submittedAt,
                    closedAt = claimStatusCard.claim.closedAt,
                    progressSegments = claimProgressData,
                    statusParagraph = claimStatusCard.claim.statusParagraph,
                )
            )
        }
    }
}
