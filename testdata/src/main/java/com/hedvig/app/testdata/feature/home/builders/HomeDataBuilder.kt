package com.hedvig.app.testdata.feature.home.builders

import com.hedvig.android.owldroid.graphql.HomeQuery

data class HomeDataBuilder(
    private val contracts: List<Status> = emptyList()
) {
    fun build() = HomeQuery.Data(
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
                    }
                )
            )
        }
    )

    enum class Status {
        PENDING
    }
}
