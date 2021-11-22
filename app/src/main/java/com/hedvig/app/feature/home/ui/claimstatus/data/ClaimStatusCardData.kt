package com.hedvig.app.feature.home.ui.claimstatus.data

import com.hedvig.android.owldroid.graphql.HomeQuery

data class ClaimStatusCardData(
    val id: String,
    val pillData: List<PillData>,
    val title: String,
    val subtitle: String,
    val claimProgressData: List<ClaimProgressData>,
) {
    companion object {
        fun fromClaimStatusCardsQuery(
            claimStatusCard: HomeQuery.ClaimStatusCard,
        ): ClaimStatusCardData {
            return ClaimStatusCardData(
                id = claimStatusCard.id,
                pillData = PillData.fromClaimStatusCardsQuery(claimStatusCard),
                title = claimStatusCard.title,
                subtitle = claimStatusCard.subtitle,
                claimProgressData = ClaimProgressData.fromClaimStatusCardsQuery(claimStatusCard)
            )
        }
    }
}
