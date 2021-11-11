package com.hedvig.app.feature.claimstatus.model

import com.hedvig.android.owldroid.graphql.ClaimStatusDetailsQuery
import com.hedvig.app.util.apollo.ThemedIconUrls
import java.time.Instant

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
        val claimType: ClaimType,
        val submittedAt: Instant,
        val closedAt: Instant?, // todo update terminology when decision is made for "Closed/handled" claims
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
                        // todo valid defaults? Make this non-nullable on the type? Optimally add it to the GraphQL type
                        ThemedIconUrls(
                            darkUrl = "/app-content-service/all_risk_dark.svg",
                            lightUrl = "/app-content-service/all_risk.svg"
                        )
                    },
                    claimType = ClaimType.fromQuery(queryModel),
                    submittedAt = queryModel.submittedAt,
                    closedAt = queryModel.closedAt,
                )
            }
        }

        sealed class ClaimType {
            object Unknown : ClaimType()
            data class Known(
                val title: String,
                val insuranceType: String,
            ) : ClaimType()

            companion object {
                fun fromQuery(queryModel: ClaimStatusDetailsQuery.ClaimStatusDetail): ClaimType {
                    val displayName = queryModel.contract?.displayName
                    // todo when claim type finally is arriving from the backend
                    return if (/*if claim type is not null &&*/false && displayName != null) {
                        Known(
                            title = String(),
                            insuranceType = displayName
                        )
                    } else {
                        Unknown
                    }
                }
            }
        }
    }
}
