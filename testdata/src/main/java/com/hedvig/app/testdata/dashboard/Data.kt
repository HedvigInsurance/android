package com.hedvig.app.testdata.dashboard

import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.testdata.common.ContractStatus
import com.hedvig.app.testdata.dashboard.builders.InsuranceDataBuilder

val INSURANCE_DATA =
    InsuranceDataBuilder(
        contracts = listOf(ContractStatus.ACTIVE)
    ).build()
val INSURANCE_DATA_STUDENT =
    InsuranceDataBuilder(
        typeOfContract = TypeOfContract.SE_APARTMENT_STUDENT_BRF,
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
