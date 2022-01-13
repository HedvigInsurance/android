package com.hedvig.app.feature.home.ui.claimstatus.data

import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.ui.compose.composables.claimprogress.ClaimProgressUiState

data class ClaimStatusCardUiState(
    val id: String,
    val pillsUiState: List<PillUiState>,
    val title: String,
    val subtitle: String,
    val claimProgressItemsUiState: List<ClaimProgressUiState>,
) {
    companion object {
        fun fromClaimStatusCardsQuery(
            claimStatusCard: HomeQuery.ClaimStatusCard,
        ): ClaimStatusCardUiState {
            return ClaimStatusCardUiState(
                id = claimStatusCard.id,
                pillsUiState = PillUiState.fromClaimStatusCardsQuery(claimStatusCard),
                title = claimStatusCard.title,
                subtitle = claimStatusCard.subtitle,
                claimProgressItemsUiState = ClaimProgressUiState.fromProgressSegments(
                    claimStatusCard.progressSegments.map { it.fragments.progressSegments }
                ),
            )
        }
    }
}
