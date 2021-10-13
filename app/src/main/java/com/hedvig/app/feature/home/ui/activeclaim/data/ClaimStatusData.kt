package com.hedvig.app.feature.home.ui.activeclaim.data

import androidx.compose.runtime.Composable
import com.hedvig.android.owldroid.graphql.HomeQuery

data class ClaimStatusData(
    val pillData: List<PillData>,
    val title: String,
    val subtitle: String,
    val claimProgressData: List<ClaimProgressData>,
) {
    companion object {
        @Composable
        fun fromHomeQueryClaimStatus(claimStatus: HomeQuery.ClaimStatus): ClaimStatusData {
            val pillData = PillData.pillDataListFromClaimStatus(claimStatus)
            val claimProgressData = ClaimProgressData.progressItemListFromClaimStatus(claimStatus)
            return ClaimStatusData(
                pillData = pillData,
                // Todo why is this nullable? Proper localization?
                title = claimStatus.contract?.displayName ?: "Unknown Type",
                // Todo what should go in the details? What is "Contents insurance" to "All-risk"
                subtitle = claimStatus.contract?.displayName ?: "Unknown Type",
                claimProgressData = claimProgressData
            )
        }
    }
}
