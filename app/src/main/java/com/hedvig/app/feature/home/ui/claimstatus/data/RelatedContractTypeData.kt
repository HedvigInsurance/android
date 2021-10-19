package com.hedvig.app.feature.home.ui.claimstatus.data

import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.util.compose.DisplayableText

data class RelatedContractTypeData(val displayableText: DisplayableText) {
    companion object {
        fun fromClaimStatus(homeQueryClaim: HomeQuery.Claim): RelatedContractTypeData {
            val relatedContract = homeQueryClaim.contract?.displayName ?: String()
            return RelatedContractTypeData(DisplayableText(relatedContract))
        }
    }
}
