package com.hedvig.app.util

import com.hedvig.android.owldroid.type.InsuranceStatus
import com.hedvig.android.owldroid.type.InsuranceType

val InsuranceType.isStudentInsurance: Boolean
    get() = when (this) {
        InsuranceType.STUDENT_RENT,
        InsuranceType.STUDENT_BRF -> true
        InsuranceType.RENT,
        InsuranceType.BRF,
        InsuranceType.HOUSE,
        InsuranceType.`$UNKNOWN` -> false
    }

val InsuranceType.isApartmentOwner: Boolean
    get() = when (this) {
        InsuranceType.BRF,
        InsuranceType.STUDENT_BRF -> true
        InsuranceType.RENT,
        InsuranceType.STUDENT_RENT,
        InsuranceType.HOUSE,
        InsuranceType.`$UNKNOWN` -> false
    }

val InsuranceType.isHouse: Boolean
    get() = when (this) {
        InsuranceType.HOUSE -> true
        else -> false
    }

val InsuranceStatus.isSigned: Boolean
    get() = when (this) {
        InsuranceStatus.ACTIVE,
        InsuranceStatus.INACTIVE_WITH_START_DATE,
        InsuranceStatus.INACTIVE,
        InsuranceStatus.TERMINATED -> true
        else -> false
    }
