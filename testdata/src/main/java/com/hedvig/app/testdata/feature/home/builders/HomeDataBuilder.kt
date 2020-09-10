package com.hedvig.app.testdata.feature.home.builders

import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.testdata.common.ContractStatus
import java.time.LocalDate

data class HomeDataBuilder(
    private val contracts: List<ContractStatus> = emptyList(),
    private val firstName: String? = "Test",
    private val commonClaims: List<HomeQuery.CommonClaim> = listOf(
        CommonClaimBuilder(
            title = "Det är kris!",
            variant = CommonClaimBuilder.Variant.EMERGENCY
        ).build(),
        CommonClaimBuilder(title = "Trasig telefon").build(),
        CommonClaimBuilder(title = "Försenat bagage").build()
    ),
    private val importantMessages: List<HomeQuery.ImportantMessage> = emptyList()
) {
    fun build() = HomeQuery.Data(
        member = HomeQuery.Member(
            firstName = firstName
        ),
        contracts = contracts.map { c ->
            HomeQuery.Contract(
                switchedFromInsuranceProvider = null,
                status = HomeQuery.Status(
                    asPendingStatus = if (c == ContractStatus.PENDING) {
                        HomeQuery.AsPendingStatus(
                            pendingSince = null
                        )
                    } else {
                        null
                    },
                    asActiveInFutureStatus = when (c) {
                        ContractStatus.ACTIVE_IN_FUTURE -> HomeQuery.AsActiveInFutureStatus(
                            futureInception = LocalDate.of(2025, 1, 1)
                        )
                        ContractStatus.ACTIVE_IN_FUTURE_INVALID -> HomeQuery.AsActiveInFutureStatus(
                            futureInception = null
                        )
                        else -> null
                    },
                    asActiveInFutureAndTerminatedInFutureStatus = if (c == ContractStatus.ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE) {
                        HomeQuery.AsActiveInFutureAndTerminatedInFutureStatus(
                            futureInception = LocalDate.of(2024, 1, 1)
                        )
                    } else {
                        null
                    },
                    asActiveStatus = if (c == ContractStatus.ACTIVE) {
                        HomeQuery.AsActiveStatus(
                            pastInception = LocalDate.now()
                        )
                    } else {
                        null
                    },
                    asTerminatedTodayStatus = if (c == ContractStatus.TERMINATED_TODAY) {
                        HomeQuery.AsTerminatedTodayStatus(today = LocalDate.now())
                    } else {
                        null
                    },
                    asTerminatedStatus = if (c == ContractStatus.TERMINATED) {
                        HomeQuery.AsTerminatedStatus(
                            termination = null
                        )
                    } else {
                        null
                    }
                )
            )
        },
        isEligibleToCreateClaim = contracts.any { it == ContractStatus.ACTIVE },
        commonClaims = commonClaims,
        importantMessages = importantMessages
    )
}

data class ImportantMessageBuilder(
    private val title: String = "Example PSA title",
    private val body: String = "Example PSA body",
    private val actionLabel: String = "Example PSA action",
    private val url: String = "https://www.example.com"
) {
    fun build() = HomeQuery.ImportantMessage(
        title = title,
        message = body,
        button = actionLabel,
        link = url
    )
}
