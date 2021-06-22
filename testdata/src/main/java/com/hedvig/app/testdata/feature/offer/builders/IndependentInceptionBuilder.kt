package com.hedvig.app.testdata.feature.offer.builders

import com.hedvig.android.owldroid.fragment.CurrentInsurerFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import java.time.LocalDate

class IndependentInceptionBuilder(
    val inceptions: List<OfferQuery.Inception> = listOf(
        OfferQuery.Inception(
            correspondingQuote = OfferQuery.CorrespondingQuote1(
                asCompleteQuote1 = OfferQuery.AsCompleteQuote1(
                    displayName = "Test Insurance",
                    id = "ea656f5f-40b2-4953-85d9-752b33e69e38"
                )
            ),
            startDate = LocalDate.now(),
            currentInsurer = OfferQuery.CurrentInsurer2(
                fragments = OfferQuery.CurrentInsurer2.Fragments(
                    CurrentInsurerFragment(
                        id = "currentinsurerid",
                        displayName = "Test current insurer",
                        switchable = false
                    )
                )
            )
        ),
        OfferQuery.Inception(
            correspondingQuote = OfferQuery.CorrespondingQuote1(
                asCompleteQuote1 = OfferQuery.AsCompleteQuote1(
                    displayName = "Test Insurance 2",
                    id = "ea656f5f-40b2-4953-85d9-752b33e69e37"
                )
            ),
            startDate = LocalDate.now().plusDays(3),
            currentInsurer = OfferQuery.CurrentInsurer2(
                fragments = OfferQuery.CurrentInsurer2.Fragments(
                    CurrentInsurerFragment(
                        id = "currentinsurerid2",
                        displayName = "Test current insurer 2",
                        switchable = true
                    )
                )
            )
        ),
        OfferQuery.Inception(
            correspondingQuote = OfferQuery.CorrespondingQuote1(
                asCompleteQuote1 = OfferQuery.AsCompleteQuote1(
                    displayName = "Test Insurance 3",
                    id = "ea656f5f-40b2-4953-85d9-752b33e69e36"
                )
            ),
            startDate = LocalDate.now().plusDays(5),
            currentInsurer = OfferQuery.CurrentInsurer2(
                fragments = OfferQuery.CurrentInsurer2.Fragments(
                    CurrentInsurerFragment(
                        id = "currentinsurerid3",
                        displayName = "Test current insurer 3",
                        switchable = false
                    )
                )
            )
        )
    )
) {
    fun build() = OfferQuery.Inception1(
        asIndependentInceptions = OfferQuery.AsIndependentInceptions(
            inceptions = inceptions
        ),
        asConcurrentInception = null,
    )
}
