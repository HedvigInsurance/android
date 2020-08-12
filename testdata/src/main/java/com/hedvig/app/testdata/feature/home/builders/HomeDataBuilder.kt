package com.hedvig.app.testdata.feature.home.builders

import com.hedvig.android.owldroid.graphql.HomeQuery
import java.time.LocalDate

data class HomeDataBuilder(
    private val contracts: List<Status> = emptyList(),
    private val firstName: String? = "Test"
) {
    fun build() = HomeQuery.Data(
        member = HomeQuery.Member(
            firstName = firstName
        ),
        contracts = contracts.map { c ->
            HomeQuery.Contract(
                switchedFromInsuranceProvider = null,
                status = HomeQuery.Status(
                    asPendingStatus = if (c == Status.PENDING) {
                        HomeQuery.AsPendingStatus(
                            pendingSince = null
                        )
                    } else {
                        null
                    },
                    asActiveInFutureStatus = if (c == Status.ACTIVE_IN_FUTURE) {
                        HomeQuery.AsActiveInFutureStatus(
                            futureInception = LocalDate.of(2025, 1, 1)
                        )
                    } else {
                        null
                    },
                    asActiveInFutureAndTerminatedInFutureStatus = if (c == Status.ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE) {
                        HomeQuery.AsActiveInFutureAndTerminatedInFutureStatus(
                            futureInception = LocalDate.of(2024, 1, 1)
                        )
                    } else {
                        null
                    },
                    asTerminatedStatus = if (c == Status.TERMINATED) {
                        HomeQuery.AsTerminatedStatus(
                            termination = null
                        )
                    } else {
                        null
                    }
                )
            )
        }
    )

    enum class Status {
        PENDING,
        ACTIVE_IN_FUTURE,
        ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE,
        TERMINATED
    }
}
