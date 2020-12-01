package com.hedvig.app.testdata.common.builders

import com.hedvig.android.owldroid.fragment.ContractStatusFragment
import com.hedvig.app.testdata.common.ContractStatus
import java.time.LocalDate

data class ContractStatusFragmentBuilder(
    private val status: ContractStatus
) {
    fun build() = ContractStatusFragment(
        asPendingStatus = if (status == ContractStatus.PENDING) {
            ContractStatusFragment.AsPendingStatus(
                pendingSince = null
            )
        } else {
            null
        },
        asActiveInFutureStatus = when (status) {
            ContractStatus.ACTIVE_IN_FUTURE -> ContractStatusFragment.AsActiveInFutureStatus(
                futureInception = LocalDate.of(2025, 1, 1)
            )
            ContractStatus.ACTIVE_IN_FUTURE_INVALID -> ContractStatusFragment.AsActiveInFutureStatus(
                futureInception = null
            )
            else -> null
        },
        asActiveStatus = if (status == ContractStatus.ACTIVE) {
            ContractStatusFragment.AsActiveStatus(
                pastInception = LocalDate.now()
            )
        } else {
            null
        },
        asActiveInFutureAndTerminatedInFutureStatus = if (status == ContractStatus.ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE) {
            ContractStatusFragment.AsActiveInFutureAndTerminatedInFutureStatus(
                futureInception = LocalDate.of(2024, 1, 1),
                futureTermination = LocalDate.of(2034, 1, 1)
            )
        } else {
            null
        },
        asTerminatedInFutureStatus = null,
        asTerminatedTodayStatus = if (status == ContractStatus.TERMINATED_TODAY) {
            ContractStatusFragment.AsTerminatedTodayStatus(today = LocalDate.now())
        } else {
            null
        },
        asTerminatedStatus = if (status == ContractStatus.TERMINATED) {
            ContractStatusFragment.AsTerminatedStatus(
                termination = null
            )
        } else {
            null
        }
    )
}
