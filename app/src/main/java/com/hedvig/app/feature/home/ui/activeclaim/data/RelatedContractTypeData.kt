package com.hedvig.app.feature.home.ui.activeclaim.data

import androidx.compose.runtime.Composable
import com.hedvig.android.owldroid.graphql.HomeQuery

@JvmInline
value class RelatedContractTypeData(val text: String) {
    companion object {
        @Composable
        fun fromClaimStatus(claimStatus: HomeQuery.ClaimStatus): RelatedContractTypeData {
            // TODO Is displayName appropriate?
            val relatedContract = claimStatus.contract?.displayName ?: String()
            return RelatedContractTypeData(relatedContract)
        }
    }
}
