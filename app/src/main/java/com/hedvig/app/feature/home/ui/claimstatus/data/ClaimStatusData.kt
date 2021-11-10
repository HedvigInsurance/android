package com.hedvig.app.feature.home.ui.claimstatus.data

import android.content.res.Resources
import com.hedvig.android.owldroid.graphql.HomeQuery
import java.util.Locale

data class ClaimStatusData(
    val id: String,
    val pillData: List<PillData>,
    val title: String,
    val subtitle: String,
    val claimProgressData: List<ClaimProgressData>,
) {
    companion object {
        fun fromHomeQueryClaim(
            homeQueryClaim: HomeQuery.Claim,
            resources: Resources,
            locale: Locale
        ): ClaimStatusData {
            val pillData = PillData.fromClaimStatus(homeQueryClaim, resources, locale)
            val claimProgressData = ClaimProgressData.fromHomeQueryClaim(homeQueryClaim, resources)
            val claimType = ClaimTypeData.fromHomeQueryClaim(homeQueryClaim, resources)
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
