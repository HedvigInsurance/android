package com.hedvig.app.feature.home.ui.claimstatus.data

import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.util.compose.DisplayableText

data class ClaimStatusData(
    val id: String,
    val pillData: List<PillData>,
    val title: DisplayableText,
    val subtitle: DisplayableText,
    val claimProgressData: List<ClaimProgressData>,
) {
    companion object {
        fun fromHomeQueryClaim(homeQueryClaim: HomeQuery.Claim): ClaimStatusData {
            val pillData = PillData.fromClaimStatus(homeQueryClaim)
            val claimProgressData = ClaimProgressData.claimProgressDataListFromHomeQueryClaim(homeQueryClaim)
            val claimType = ClaimTypeData.fromHomeQueryClaim(homeQueryClaim)
            val relatedContractType = RelatedContractTypeData.fromClaimStatus(homeQueryClaim)
            return ClaimStatusData(
                id = homeQueryClaim.id,
                pillData = pillData,
                title = claimType.displayableText,
                subtitle = relatedContractType.displayableText,
                claimProgressData = claimProgressData
            )
        }
    }
}
