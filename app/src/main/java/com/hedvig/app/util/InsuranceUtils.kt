package com.hedvig.app.util

import type.InsuranceStatus
import type.InsuranceType

val InsuranceType.isStudentInsurance: Boolean
    get() = when (this) {
        InsuranceType.STUDENT_RENT,
        InsuranceType.STUDENT_BRF -> true
        InsuranceType.RENT,
        InsuranceType.BRF,
        InsuranceType.UNKNOWN__ -> false
    }

val InsuranceType.isApartmentOwner: Boolean
    get() = when (this) {
        InsuranceType.BRF,
        InsuranceType.STUDENT_BRF -> true
        InsuranceType.RENT,
        InsuranceType.STUDENT_RENT,
        InsuranceType.UNKNOWN__ -> false
    }

val InsuranceStatus.isSigned: Boolean
    get() = when (this) {
        InsuranceStatus.ACTIVE,
        InsuranceStatus.INACTIVE_WITH_START_DATE,
        InsuranceStatus.INACTIVE,
        InsuranceStatus.TERMINATED -> true
        else -> false
    }
