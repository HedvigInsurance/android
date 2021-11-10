package com.hedvig.app.feature.claimstatus.model

import com.hedvig.android.owldroid.graphql.ClaimStatusDetailsQuery
import com.hedvig.app.util.apollo.ThemedIconUrls

data class ClaimStatusDetailData(
    val claimInfoData: ClaimInfoData,
) {
    companion object {
        fun fromQueryModel(
            queryModel: ClaimStatusDetailsQuery.Data,
            // TODO temporary parameter, remove when the query itself filters the item
            claimId: String,
        ): ClaimStatusDetailData {
            // TODO look into filtering on the query itself if possible instead of fetching everything for no reason
            @Suppress("NAME_SHADOWING")
            val queryModel = queryModel.claimStatusDetails.first { it.id == claimId }
            return ClaimStatusDetailData(
                claimInfoData = ClaimInfoData.fromQuery(queryModel)
            )
        }
    }

    data class ClaimInfoData(
        val themedIconUrls: ThemedIconUrls,
        val claimType: String,
        val insuranceType: String,
    ) {
        companion object {
            fun fromQuery(
                queryModel: ClaimStatusDetailsQuery.ClaimStatusDetail,
            ): ClaimInfoData {
                return ClaimInfoData(
                    themedIconUrls = if (queryModel.contract != null) {
                        ThemedIconUrls.from(
                            queryModel.contract!!.contractPerils.first().icon.variants.fragments.iconVariantsFragment
                        )
                    } else {
                        // todo valid defaults? Make this non-nullable on the type?
                        ThemedIconUrls(
                            darkUrl = "/app-content-service/all_risk_dark.svg",
                            lightUrl = "/app-content-service/all_risk.svg"
                        )
                    },
                    claimType = "Some Claim Type", // todo when claim type finally is arriving from the backend
                    insuranceType = queryModel.contract?.displayName ?: String(),
                )
            }
        }
    }
}
