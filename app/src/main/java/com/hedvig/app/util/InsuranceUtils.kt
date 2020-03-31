package com.hedvig.app.util

import com.hedvig.android.owldroid.type.InsuranceStatus

val InsuranceStatus.isSigned: Boolean
    get() = when (this) {
        InsuranceStatus.ACTIVE,
        InsuranceStatus.INACTIVE_WITH_START_DATE,
        InsuranceStatus.INACTIVE,
        InsuranceStatus.TERMINATED -> true
        else -> false
    }
