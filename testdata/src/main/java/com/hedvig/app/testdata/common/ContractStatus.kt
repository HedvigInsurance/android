package com.hedvig.app.testdata.common

enum class ContractStatus {
    PENDING,
    ACTIVE_IN_FUTURE,
    ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE,
    ACTIVE_IN_FUTURE_INVALID,
    ACTIVE,
    TERMINATED_TODAY,
    TERMINATED,
    TERMINATED_IN_FUTURE;

    fun toTypename() = when (this) {
        PENDING -> "PendingStatus"
        ACTIVE_IN_FUTURE, ACTIVE_IN_FUTURE_INVALID -> "ActiveInFutureStatus"
        ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE -> "ActiveInFutureAndTerminatedInFutureStatus"
        ACTIVE -> "ActiveStatus"
        TERMINATED_TODAY -> "TerminatedTodayStatus"
        TERMINATED -> "TerminatedStatus"
        TERMINATED_IN_FUTURE -> "TerminatedInFutureStatus"
    }
}
