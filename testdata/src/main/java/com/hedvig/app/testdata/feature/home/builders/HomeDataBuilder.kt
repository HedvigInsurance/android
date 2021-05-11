package com.hedvig.app.testdata.feature.home.builders

import com.hedvig.android.owldroid.fragment.IconVariantsFragment
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
    private val importantMessages: List<HomeQuery.ImportantMessage> = emptyList(),
    private val renewalDate: LocalDate? = null,
    private val pendingQuotesList: List<HomeQuery.Quote> = emptyList()
) {
    fun build() = HomeQuery.Data(
        member = HomeQuery.Member(
            firstName = firstName
        ),
        contracts = contracts.map { c ->
            HomeQuery.Contract(
                displayName = CONTRACT_DISPLAY_NAME,
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
                    asActiveInFutureAndTerminatedInFutureStatus = if (
                        c == ContractStatus.ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE
                    ) {
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
                    },
                    asTerminatedInFutureStatus = if (c == ContractStatus.TERMINATED_IN_FUTURE) {
                        HomeQuery.AsTerminatedInFutureStatus(
                            futureTermination = LocalDate.now().plusDays(10)
                        )
                    } else {
                        null
                    }
                ),
                upcomingRenewal = if (renewalDate != null) {
                    HomeQuery.UpcomingRenewal(
                        renewalDate = renewalDate,
                        draftCertificateUrl = "https://www.example.com"
                    )
                } else {
                    null
                }
            )
        },
        isEligibleToCreateClaim = contracts.any { it == ContractStatus.ACTIVE },
        commonClaims = commonClaims,
        importantMessages = importantMessages,
        howClaimsWork = listOf(
            HomeQuery.HowClaimsWork(
                illustration = HomeQuery.Illustration(
                    variants = HomeQuery.Variants2(
                        fragments = HomeQuery.Variants2.Fragments(
                            IconVariantsFragment(
                                dark = IconVariantsFragment.Dark(
                                    svgUrl = "/app-content-service/welcome_welcome.svg"
                                ),
                                light = IconVariantsFragment.Light(
                                    svgUrl = "/app-content-service/welcome_welcome.svg"
                                )
                            )
                        )
                    )
                ),
                body = "1"
            ),
            HomeQuery.HowClaimsWork(
                illustration = HomeQuery.Illustration(
                    variants = HomeQuery.Variants2(
                        fragments = HomeQuery.Variants2.Fragments(
                            IconVariantsFragment(
                                dark = IconVariantsFragment.Dark(
                                    svgUrl = "/app-content-service/welcome_welcome.svg"
                                ),
                                light = IconVariantsFragment.Light(
                                    svgUrl = "/app-content-service/welcome_welcome.svg"
                                )
                            )
                        )
                    )
                ),
                body = "2"
            ),
            HomeQuery.HowClaimsWork(
                illustration = HomeQuery.Illustration(
                    variants = HomeQuery.Variants2(
                        fragments = HomeQuery.Variants2.Fragments(
                            IconVariantsFragment(
                                dark = IconVariantsFragment.Dark(
                                    svgUrl = "/app-content-service/welcome_welcome.svg"
                                ),
                                light = IconVariantsFragment.Light(
                                    svgUrl = "/app-content-service/welcome_welcome.svg"
                                )
                            )
                        )
                    )
                ),
                body = "3"
            )
        ),
        quoteBundle = HomeQuery.QuoteBundle(quotes = pendingQuotesList)
    )

    companion object {
        const val CONTRACT_DISPLAY_NAME = "Home Insurance"
    }
}

data class ImportantMessageBuilder(
    private val body: String = "Example PSA body",
    private val url: String = "https://www.example.com"
) {
    fun build() = HomeQuery.ImportantMessage(
        message = body,
        link = url
    )
}
