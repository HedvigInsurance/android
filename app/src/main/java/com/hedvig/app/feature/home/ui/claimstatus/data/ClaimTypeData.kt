package com.hedvig.app.feature.home.ui.claimstatus.data

import android.content.res.Resources
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.ClaimStatus
import com.hedvig.app.R

data class ClaimTypeData(val text: String) {
    companion object {
        fun fromHomeQueryClaim(homeQueryClaim: HomeQuery.Claim, resources: Resources): ClaimTypeData {
            return ClaimTypeData(
                // TODO add the claim type title when the copy is addressed https://hedvig.atlassian.net/browse/APP-995
                when (homeQueryClaim.status) {
                    ClaimStatus.SUBMITTED -> resources.getString(R.string.claim_casetype_new_insurance_case)
                    else -> resources.getString(R.string.claim_casetype_insurance_case)
                }
            )
        }
    }
}
