package com.hedvig.app.feature.home.ui.claimstatus.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.ClaimStatus
import com.hedvig.app.R

@JvmInline
value class ClaimTypeData(val text: String) {
    companion object {
        @Composable
        fun fromHomeQueryClaim(homeQueryClaim: HomeQuery.Claim): ClaimTypeData {
            return ClaimTypeData(
                when {
                    // TODO get the type from backend? It exists in Hope already, but not on GraphQL
                    //  context: https://hedviginsurance.slack.com/archives/C01SCHY7W1W/p1634557500017900
                    // claimStatus.perilType != null -> claimStatus.perilType.toProperReadableString
                    homeQueryClaim.status == ClaimStatus.SUBMITTED -> {
                        stringResource(R.string.claim_type_new_insurance_case)
                    }
                    else -> {
                        stringResource(R.string.claim_type_insurance_case)
                    }
                }
            )
        }
    }
}
