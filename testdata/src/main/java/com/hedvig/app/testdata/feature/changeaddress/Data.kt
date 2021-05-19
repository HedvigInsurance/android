package com.hedvig.app.testdata.feature.changeaddress

import com.hedvig.android.owldroid.fragment.UpcomingAgreementFragment
import com.hedvig.android.owldroid.graphql.SelfChangeEligibilityQuery
import com.hedvig.android.owldroid.graphql.UpcomingAgreementQuery
import com.hedvig.app.testdata.feature.changeaddress.builders.SelfChangeEligibilityBuilder
import com.hedvig.app.testdata.feature.changeaddress.builders.UpcomingAgreementBuilder

val UPCOMING_AGREEMENT_NONE = UpcomingAgreementQuery.Data(
    contracts = listOf(
        UpcomingAgreementQuery.Contract(
            fragments = UpcomingAgreementQuery.Contract.Fragments(
                upcomingAgreementFragment = UpcomingAgreementFragment(
                    upcomingAgreementDetailsTable = UpcomingAgreementFragment.UpcomingAgreementDetailsTable(
                        title = "",
                        sections = listOf()
                    ),
                    status = UpcomingAgreementFragment.Status(
                        asActiveStatus = null,
                        asTerminatedInFutureStatus = null,
                        asTerminatedTodayStatus = null,
                    ),
                )
            )
        )
    )
)

val UPCOMING_AGREEMENT_SWEDISH_APARTMENT = UpcomingAgreementQuery.Data(
    contracts = listOf(
        UpcomingAgreementQuery.Contract(
            fragments = UpcomingAgreementQuery.Contract.Fragments(
                upcomingAgreementFragment = UpcomingAgreementBuilder().build()
            ),
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
