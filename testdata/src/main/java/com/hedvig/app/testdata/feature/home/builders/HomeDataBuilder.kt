package com.hedvig.app.testdata.feature.home.builders

import com.hedvig.android.owldroid.fragment.IconVariantsFragment
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.ClaimStatus
import com.hedvig.android.owldroid.type.ClaimStatusCardPillType
import com.hedvig.android.owldroid.type.ClaimStatusProgressType
import com.hedvig.app.testdata.common.ContractStatus
import java.time.Instant
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
    private val withClaimStatusCards: Boolean = false,
    private val importantMessages: List<HomeQuery.ImportantMessage> = emptyList(),
    private val renewalDate: LocalDate? = null,
) {
    fun build() = HomeQuery.Data(
        member = HomeQuery.Member(
            firstName = firstName
        ),
        claimStatusCards = if (withClaimStatusCards) { // Better preview the cards using Showkase
            ClaimStatusCardsBuilder().build()
        } else {
            emptyList()
        },
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
        )
    )

    companion object {
        const val CONTRACT_DISPLAY_NAME = "Home Insurance"
    }
}

data class ImportantMessageBuilder(
    private val body: String = "Example PSA body",
    private val url: String = "https://www.example.com",
) {
    fun build() = HomeQuery.ImportantMessage(
        message = body,
        link = url
    )
}

class ClaimStatusCardsBuilder {
    fun build(): List<HomeQuery.ClaimStatusCard> {
        return List(2) { index ->
            HomeQuery.ClaimStatusCard(
                id = index.toString(),
                pills = List(3) {
                    HomeQuery.Pill(text = "Pill #$it", type = ClaimStatusCardPillType.values().random())
                },
                title = "Title. Random pills ^^",
                subtitle = "Subtitle. Random progress segments vv",
                progressSegments = List(3) {
                    HomeQuery.ProgressSegment(
                        fragments = HomeQuery.ProgressSegment.Fragments(
                            progressSegments = com.hedvig.android.owldroid.fragment.ProgressSegments(
                                text = "Segment #$it",
                                type = ClaimStatusProgressType.values().random()
                            )
                        )
                    )
                },
                claim = HomeQuery.Claim(
                    submittedAt = Instant.now(),
                    closedAt = null,
                    type = "Claim type",
                    statusParagraph = "Status Paragraph",
                    progressSegments = emptyList(),
                    status = ClaimStatus.CLOSED
                ),
            )
        }
    }
}
