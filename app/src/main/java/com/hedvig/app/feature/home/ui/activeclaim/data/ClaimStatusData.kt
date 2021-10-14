package com.hedvig.app.feature.home.ui.activeclaim.data

import androidx.compose.runtime.Composable
import com.hedvig.android.owldroid.graphql.HomeQuery

data class ClaimStatusData(
    val id: String,
    val pillData: List<PillData>,
    val title: String,
    val subtitle: String,
    val claimProgressData: List<ClaimProgressData>,
) {
    companion object {
        @Composable
        fun fromHomeQueryClaimStatus(claimStatus: HomeQuery.ClaimStatus): ClaimStatusData {
            val pillData = PillData.fromClaimStatus(claimStatus)
            val claimProgressData = ClaimProgressData.progressItemListFromClaimStatus(claimStatus)
            val claimType = ClaimTypeData.fromClaimStatus(claimStatus)
            val relatedContractType = RelatedContractTypeData.fromClaimStatus(claimStatus)
            return ClaimStatusData(
                id = claimStatus.id,
                pillData = pillData,
                title = claimType.text,
                subtitle = relatedContractType.text,
                claimProgressData = claimProgressData
            )
        }
    }
}
