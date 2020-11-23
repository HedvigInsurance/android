package com.hedvig.app.testdata.feature.insurance

import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.testdata.feature.insurance.builders.InsuranceDataBuilder

val INSURANCE_DATA_SWEDISH_APARTMENT = InsuranceDataBuilder().build()
val INSURANCE_DATA_SWEDISH_APARTMENT_NO_RENEWAL = InsuranceDataBuilder(renewal = null).build()
val INSURANCE_DATA_SWEDISH_HOUSE = InsuranceDataBuilder(type = TypeOfContract.SE_HOUSE).build()
val INSURANCE_DATA_NORWEGIAN_TRAVEL = InsuranceDataBuilder(type = TypeOfContract.NO_TRAVEL).build()
val INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS =
    InsuranceDataBuilder(type = TypeOfContract.NO_HOME_CONTENT_RENT).build()
val INSURANCE_DATA_DANISH_TRAVEL = InsuranceDataBuilder(type = TypeOfContract.DK_TRAVEL).build()
