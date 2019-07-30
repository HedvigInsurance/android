package com.hedvig.app.util

import com.hedvig.android.owldroid.type.InsuranceType

fun isStudentInsurance(insuranceType: InsuranceType) = when (insuranceType) {
    InsuranceType.STUDENT_RENT,
    InsuranceType.STUDENT_BRF -> true
    InsuranceType.RENT,
    InsuranceType.BRF,
    InsuranceType.`$UNKNOWN` -> false
}

fun isApartmentOwner(insuranceType: InsuranceType) = when (insuranceType) {
    InsuranceType.BRF,
    InsuranceType.STUDENT_BRF -> true
    InsuranceType.RENT,
    InsuranceType.STUDENT_RENT,
    InsuranceType.`$UNKNOWN` -> false
}
