package com.hedvig.app.feature.home.ui.claimstatus.data

import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.ClaimStatus
import com.hedvig.app.R
import com.hedvig.app.util.compose.DisplayableText

data class ClaimTypeData(val displayableText: DisplayableText) {
    companion object {
        fun fromHomeQueryClaim(homeQueryClaim: HomeQuery.Claim): ClaimTypeData {
            return ClaimTypeData(
                when {
                    // TODO get the type from backend? It exists in Hope already, but not on GraphQL
                    //  context: https://hedviginsurance.slack.com/archives/C01SCHY7W1W/p1634557500017900
                    // claimStatus.perilType != null -> claimStatus.perilType.toProperReadableString
                    homeQueryClaim.status == ClaimStatus.SUBMITTED -> {
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
