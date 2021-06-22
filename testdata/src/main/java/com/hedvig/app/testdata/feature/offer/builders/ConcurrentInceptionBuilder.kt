package com.hedvig.app.testdata.feature.offer.builders

import com.hedvig.android.owldroid.fragment.CurrentInsurerFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import java.time.LocalDate

class ConcurrentInceptionBuilder(
    val quoteIds: List<String> = listOf("ea656f5f-40b2-4953-85d9-752b33e69e38"),
    val startDate: LocalDate = LocalDate.now(),
    val currentInsurer: OfferQuery.CurrentInsurer1 = OfferQuery.CurrentInsurer1(
        fragments = OfferQuery.CurrentInsurer1.Fragments(
            currentInsurerFragment = CurrentInsurerFragment(
                id = "currentinsurerid",
                displayName = "Test current insurer",
                switchable = false
            )
        )
    )
) {
    fun build() = OfferQuery.Inception1(
        asConcurrentInception = OfferQuery.AsConcurrentInception(
            correspondingQuotes = quoteIds.map {
                OfferQuery.CorrespondingQuote(
                    asCompleteQuote = OfferQuery.AsCompleteQuote(
                        id = it
                    ),
                    asIncompleteQuote = null
                )
            },
            startDate = startDate,
            currentInsurer = currentInsurer
        ),
        asIndependentInceptions = null
    )
}
