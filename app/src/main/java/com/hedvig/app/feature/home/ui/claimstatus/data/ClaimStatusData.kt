package com.hedvig.app.feature.home.ui.claimstatus.data

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
        fun fromHomeQueryClaim(homeQueryClaim: HomeQuery.Claim): ClaimStatusData {
            val pillData = PillData.fromClaimStatus(homeQueryClaim)
            val claimProgressData = ClaimProgressData.claimProgressDataListFromHomeQueryClaim(homeQueryClaim)
            val claimType = ClaimTypeData.fromHomeQueryClaim(homeQueryClaim)
            val relatedContractType = RelatedContractTypeData.fromClaimStatus(homeQueryClaim)
            return ClaimStatusData(
                id = homeQueryClaim.id,
                pillData = pillData,
                title = claimType.text,
                subtitle = relatedContractType.text,
                claimProgressData = claimProgressData
            )
        }
    }
}
