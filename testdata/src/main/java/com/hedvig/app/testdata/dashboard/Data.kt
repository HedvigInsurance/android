package com.hedvig.app.testdata.dashboard

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.testdata.dashboard.builders.InsuranceDataBuilder
import java.time.LocalDate

val DASHBOARD_DATA_WITH_RENEWAL_DATE = InsuranceDataBuilder(
    InsuranceQuery.UpcomingRenewal(renewalDate = LocalDate.now(),
        draftCertificateUrl = "https://www.example.com")).build()
