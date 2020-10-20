package com.hedvig.app.testdata.feature.insurance

import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.testdata.feature.insurance.builders.InsuranceDataBuilder

val INSURANCE_DATA_SWEDISH_APARTMENT = InsuranceDataBuilder().build()

val INSURANCE_DATA_SWEDISH_APARTMENT_NO_RENEWAL = InsuranceDataBuilder(renewal = null).build()

val INSURANCE_DATA_NORWEGIAN_TRAVEL = InsuranceDataBuilder(type = TypeOfContract.NO_TRAVEL).build()
