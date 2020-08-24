package com.hedvig.app.testdata.dashboard

import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.app.testdata.dashboard.builders.DashboardDataBuilder
import java.time.LocalDate

val DASHBOARD_DATA_WITH_RENEWAL_DATE = DashboardDataBuilder(
    DashboardQuery.UpcomingRenewal(renewalDate = LocalDate.now(),
        draftCertificateUrl = "https://www.example.com")).build()
