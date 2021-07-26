package com.hedvig.app.testdata.feature.offer.builders

import com.hedvig.android.owldroid.fragment.CurrentInsurerFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import java.time.LocalDate

class IndependentInceptionBuilder(
    val startDateFromPreviousInsurer: Boolean = false,
    val withCurrentInsurer: Boolean = true
) {
    fun build() = OfferQuery.Inception1(
        asIndependentInceptions = OfferQuery.AsIndependentInceptions(
            inceptions = listOf(
                OfferQuery.Inception(
                    correspondingQuote = OfferQuery.CorrespondingQuote1(
                        asCompleteQuote1 = OfferQuery.AsCompleteQuote1(
                            displayName = "Test Insurance",
                            id = "ea656f5f-40b2-4953-85d9-752b33e69e38"
                        )
                    ),
                    startDate = if (startDateFromPreviousInsurer) null else LocalDate.now(),
                    currentInsurer = if (withCurrentInsurer) {
                        OfferQuery.CurrentInsurer2(
                            fragments = OfferQuery.CurrentInsurer2.Fragments(
                                CurrentInsurerFragment(
                                    id = "currentinsurerid",
                                    displayName = "Test current insurer",
                                    switchable = false
                                )
                            )
                        )
                    } else null
                ),
                OfferQuery.Inception(
                    correspondingQuote = OfferQuery.CorrespondingQuote1(
                        asCompleteQuote1 = OfferQuery.AsCompleteQuote1(
                            displayName = "Test Insurance 2",
                            id = "ea656f5f-40b2-4953-85d9-752b33e69e37"
                        )
                    ),
                    startDate = if (startDateFromPreviousInsurer) null else LocalDate.now().plusDays(3),
                    currentInsurer = if (withCurrentInsurer) {
                        OfferQuery.CurrentInsurer2(
                            fragments = OfferQuery.CurrentInsurer2.Fragments(
                                CurrentInsurerFragment(
                                    id = "currentinsurerid2",
                                    displayName = "Test current insurer 2",
                                    switchable = true
                                )
                            )
                        )
                    } else null
                ),
                OfferQuery.Inception(
                    correspondingQuote = OfferQuery.CorrespondingQuote1(
                        asCompleteQuote1 = OfferQuery.AsCompleteQuote1(
                            displayName = "Test Insurance 3",
                            id = "ea656f5f-40b2-4953-85d9-752b33e69e36"
                        )
                    ),
                    startDate = if (startDateFromPreviousInsurer) null else LocalDate.now().plusDays(5),
                    currentInsurer = if (withCurrentInsurer) {
                        OfferQuery.CurrentInsurer2(
                            fragments = OfferQuery.CurrentInsurer2.Fragments(
                                CurrentInsurerFragment(
                                    id = "currentinsurerid3",
                                    displayName = "Test current insurer 3",
                                    switchable = false
                                )
                            )
                        )
                    } else null
                )
            )
        ),
        asConcurrentInception = null,
    )
}
