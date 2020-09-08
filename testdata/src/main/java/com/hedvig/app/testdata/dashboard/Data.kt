package com.hedvig.app.testdata.dashboard

import com.hedvig.app.testdata.dashboard.builders.InsuranceDataBuilder

val INSURANCE_DATA = InsuranceDataBuilder().build()

val INSURANCE_DATA_NO_RENEWAL = InsuranceDataBuilder(renewal = null).build()
