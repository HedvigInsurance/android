package com.hedvig.app.testdata.feature.changeaddress

import com.hedvig.android.owldroid.graphql.ActiveContractBundlesQuery
import com.hedvig.android.owldroid.graphql.UpcomingAgreementQuery
import com.hedvig.app.testdata.feature.changeaddress.builders.ActiveContractBundlesBuilder
import com.hedvig.app.testdata.feature.changeaddress.builders.UpcomingAgreementBuilder

val UPCOMING_AGREEMENT_NONE = UpcomingAgreementQuery.Data(
    contracts = listOf(
        UpcomingAgreementQuery.Contract(
            status = UpcomingAgreementQuery.Status(
                asActiveStatus = null,
                asTerminatedInFutureStatus = null,
                asTerminatedTodayStatus = null,
            )
        )
    )
)

val UPCOMING_AGREEMENT_SWEDISH_HOUSE = UpcomingAgreementQuery.Data(
    contracts = listOf(
        UpcomingAgreementQuery.Contract(
            status = UpcomingAgreementQuery.Status(
                asActiveStatus = UpcomingAgreementQuery.AsActiveStatus(
                    upcomingAgreementChange = UpcomingAgreementBuilder().build()
                ),
                asTerminatedInFutureStatus = null,
                asTerminatedTodayStatus = null,
            )
        )
    )
)

val SELF_CHANGE_ELIGIBILITY = ActiveContractBundlesQuery.Data(
    listOf(
        ActiveContractBundlesBuilder(
            embarkStoryId = "testId"
        ).build()
    )
)

val BLOCKED_SELF_CHANGE_ELIGIBILITY = ActiveContractBundlesQuery.Data(
    listOf()
)
