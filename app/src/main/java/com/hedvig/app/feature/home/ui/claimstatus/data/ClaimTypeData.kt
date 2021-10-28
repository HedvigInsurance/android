package com.hedvig.app.feature.home.ui.claimstatus.data

import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.ClaimStatus
import com.hedvig.app.R
import com.hedvig.app.util.compose.DisplayableText

data class ClaimTypeData(val displayableText: DisplayableText) {
    companion object {
        fun fromHomeQueryClaim(homeQueryClaim: HomeQuery.Claim): ClaimTypeData {
            return ClaimTypeData(
                // TODO add the claim type title when the copy is addressed https://hedvig.atlassian.net/browse/APP-995
                when (homeQueryClaim.status) {
                    ClaimStatus.SUBMITTED -> {
                        DisplayableText(R.string.claim_type_new_insurance_case)
                    }
                    else -> {
                        DisplayableText(R.string.claim_type_insurance_case)
                    }
                }
            )
        }
    }
}
