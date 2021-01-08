package com.hedvig.app.testdata.feature.insurance

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.testdata.feature.insurance.builders.InsuranceContractBuilder

val INSURANCE_DATA_SWEDISH_APARTMENT =
    InsuranceQuery.Data(contracts = listOf(InsuranceContractBuilder().build()))
val INSURANCE_DATA_SWEDISH_APARTMENT_NO_RENEWAL =
    InsuranceQuery.Data(contracts = listOf(InsuranceContractBuilder(renewal = null).build()))
val INSURANCE_DATA_SWEDISH_HOUSE =
    InsuranceQuery.Data(contracts = listOf(InsuranceContractBuilder(type = TypeOfContract.SE_HOUSE).build()))
val INSURANCE_DATA_NORWEGIAN_TRAVEL =
    InsuranceQuery.Data(contracts = listOf(InsuranceContractBuilder(type = TypeOfContract.NO_TRAVEL).build()))
val INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS =
    InsuranceQuery.Data(contracts = listOf(InsuranceContractBuilder(type = TypeOfContract.NO_HOME_CONTENT_RENT).build()))
val INSURANCE_DATA_DANISH_TRAVEL =
    InsuranceQuery.Data(contracts = listOf(InsuranceContractBuilder(type = TypeOfContract.DK_TRAVEL).build()))
val INSURANCE_DATA_MULTIPLE_DANISH_CONTRACTS = InsuranceQuery.Data(
    contracts = listOf(
        InsuranceContractBuilder(type = TypeOfContract.DK_HOME_CONTENT_OWN).build(),
        InsuranceContractBuilder(type = TypeOfContract.DK_TRAVEL).build(),
        InsuranceContractBuilder(type = TypeOfContract.DK_ACCIDENT).build(),
    )
)
