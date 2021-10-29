package com.hedvig.app.feature.home.ui.claimstatus.data

import com.hedvig.android.owldroid.graphql.HomeQuery

data class RelatedContractTypeData(val text: String) {
    companion object {
        fun fromClaimStatus(homeQueryClaim: HomeQuery.Claim): RelatedContractTypeData {
            val relatedContract = homeQueryClaim.contract?.displayName ?: String()
            return RelatedContractTypeData(relatedContract)
        }
    }
}
