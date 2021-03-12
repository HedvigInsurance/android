package com.hedvig.app.feature.home.ui.changeaddress

import com.hedvig.android.owldroid.fragment.UpcomingAgreementFragment
import com.hedvig.android.owldroid.graphql.UpcomingAgreementQuery

fun UpcomingAgreementQuery.Contract.upcomingAgreementChange(): UpcomingAgreementFragment? {
    return status.asActiveStatus?.upcomingAgreementChange?.fragments?.upcomingAgreementFragment
        ?: status.asTerminatedInFutureStatus?.upcomingAgreementChange?.fragments?.upcomingAgreementFragment
        ?: status.asTerminatedTodayStatus?.upcomingAgreementChange?.fragments?.upcomingAgreementFragment
}
