package com.hedvig.app.testdata.dashboard

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.testdata.common.ContractStatus
import com.hedvig.app.testdata.dashboard.builders.InsuranceDataBuilder

val INSURANCE_DATA =
    InsuranceDataBuilder(
        contracts = listOf(ContractStatus.ACTIVE)
    ).build()

val INSURANCE_DATA_STUDENT =
    InsuranceDataBuilder(
        contracts = listOf(ContractStatus.ACTIVE),
        displayName = "Hemförsäkring Student"
    ).build()

val INSURANCE_DATA_ACTIVE_AND_TERMINATED =
    InsuranceDataBuilder(
        contracts = listOf(ContractStatus.ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE)
    ).build()
val INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED = InsuranceDataBuilder(
    contracts = listOf(ContractStatus.ACTIVE, ContractStatus.TERMINATED)
).build()
val INSURANCE_DATA_DANISH_HOME_CONTENTS = InsuranceDataBuilder(
    contracts = listOf(ContractStatus.ACTIVE),
).build()
val INSURANCE_DATA_DANISH_ACCIDENT = InsuranceDataBuilder(
    contracts = listOf(ContractStatus.ACTIVE),
).build()
val INSURANCE_DATA_TERMINATED = InsuranceDataBuilder(
    contracts = listOf(ContractStatus.TERMINATED)
).build()

val INSURANCE_DATA_WITH_CROSS_SELL = InsuranceDataBuilder(
    contracts = listOf(ContractStatus.ACTIVE),
    crossSells = listOf(
        InsuranceQuery.PotentialCrossSell(
            title = "Accident Insurance",
            description = "179 SEK/mo.",
            callToAction = "Calculate price",
            action = InsuranceQuery.Action(
                __typename = "CrossSellEmbark",
                asCrossSellEmbark = InsuranceQuery.AsCrossSellEmbark(
                    embarkStory = InsuranceQuery.EmbarkStory(
                        name = "123",
                    ),
                )
            ),
            blurHash = "LJC6\$2-:DiWB~WxuRkayMwNGo~of",
            imageUrl = "https://images.unsplash.com/photo-1628996796855-0b056a464e06",
        )
    )
).build()

val INSURANCE_DATA_UPCOMING_AGREEMENT = InsuranceDataBuilder(
    contracts = listOf(ContractStatus.ACTIVE),
    showUpcomingAgreement = true,
).build()
