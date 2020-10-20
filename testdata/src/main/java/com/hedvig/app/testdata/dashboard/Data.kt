package com.hedvig.app.testdata.dashboard

import com.hedvig.android.owldroid.fragment.ContractStatusFragment
import com.hedvig.app.testdata.dashboard.builders.InsuranceDataBuilder
import java.time.LocalDate

val INSURANCE_DATA = InsuranceDataBuilder().build()
val INSURANCE_DATA_ACTIVE_AND_TERMINATED = InsuranceDataBuilder(
    activeStatus = null,
    activeInFutureAndTerminatedInFutureStatus = ContractStatusFragment.AsActiveInFutureAndTerminatedInFutureStatus(
        futureInception = LocalDate.of(2050, 1,1),
        futureTermination = LocalDate.of(2060, 1,1)
    )
).build()
val INSURANCE_DATA_NO_RENEWAL = InsuranceDataBuilder(renewal = null).build()
