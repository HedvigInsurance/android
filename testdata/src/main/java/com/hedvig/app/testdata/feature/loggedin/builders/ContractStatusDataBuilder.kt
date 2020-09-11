package com.hedvig.app.testdata.feature.loggedin.builders

import com.hedvig.android.owldroid.graphql.ContractStatusQuery
import com.hedvig.app.testdata.common.ContractStatus

data class ContractStatusDataBuilder(
    private val statuses: List<ContractStatus> = emptyList()
) {
    fun build() = ContractStatusQuery.Data(
        contracts = statuses.map { cs ->
            ContractStatusQuery.Contract(
                status = ContractStatusQuery.Status(
                    __typename = when (cs) {
                        ContractStatus.PENDING -> "PendingStatus"
                        ContractStatus.ACTIVE_IN_FUTURE -> "ActiveInFutureStatus"
                        ContractStatus.ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE, ContractStatus.ACTIVE_IN_FUTURE_INVALID -> "ActiveInFutureAndTerminatedInFutureStatus"
                        ContractStatus.ACTIVE -> "ActiveStatus"
                        ContractStatus.TERMINATED_TODAY -> "TerminatedTodayStatus"
                        ContractStatus.TERMINATED -> "TerminatedStatus"
                    }
                )
            )
        }
    )
}
