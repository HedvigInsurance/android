package com.hedvig.app.feature.home.ui.claimstatus.data

import androidx.compose.runtime.Composable
import com.hedvig.android.owldroid.graphql.HomeQuery

@JvmInline
value class RelatedContractTypeData(val text: String) {
    companion object {
        @Composable
        fun fromClaimStatus(homeQueryClaim: HomeQuery.Claim): RelatedContractTypeData {
            // TODO Is displayName appropriate?
            val relatedContract = homeQueryClaim.contract?.displayName ?: String()
            return RelatedContractTypeData(relatedContract)
        }
    }
}
