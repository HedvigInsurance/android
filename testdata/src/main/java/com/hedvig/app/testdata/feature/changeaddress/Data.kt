package com.hedvig.app.testdata.feature.changeaddress

import com.hedvig.android.owldroid.graphql.SelfChangeEligibilityQuery
import com.hedvig.android.owldroid.graphql.UpcomingAgreementQuery
import com.hedvig.app.testdata.feature.changeaddress.builders.SelfChangeEligibilityBuilder
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

val SELF_CHANGE_ELIGIBILITY = SelfChangeEligibilityQuery.Data(
    SelfChangeEligibilityBuilder(
        embarkStoryId = "testId"
    ).build()
)

val BLOCKED_SELF_CHANGE_ELIGIBILITY = SelfChangeEligibilityQuery.Data(
    SelfChangeEligibilityBuilder().build()
)
