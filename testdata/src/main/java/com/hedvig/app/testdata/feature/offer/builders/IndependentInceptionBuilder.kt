package com.hedvig.app.testdata.feature.offer.builders

import com.hedvig.android.owldroid.fragment.CurrentInsurerFragment
import com.hedvig.android.owldroid.fragment.QuoteBundleFragment
import java.time.LocalDate

class IndependentInceptionBuilder(
    val startDateFromPreviousInsurer: Boolean = false,
    val withCurrentInsurer: Boolean = true
) {
    fun build() = QuoteBundleFragment.Inception1(
        asIndependentInceptions = QuoteBundleFragment.AsIndependentInceptions(
            inceptions = listOf(
                QuoteBundleFragment.Inception(
                    correspondingQuoteId = "ea656f5f-40b2-4953-85d9-752b33e69e38",
                    startDate = if (startDateFromPreviousInsurer) null else LocalDate.now(),
                    currentInsurer = if (withCurrentInsurer) {
                        QuoteBundleFragment.CurrentInsurer2(
                            fragments = QuoteBundleFragment.CurrentInsurer2.Fragments(
                                CurrentInsurerFragment(
                                    id = "currentinsurerid",
                                    displayName = "Test current insurer",
                                    switchable = false
                                )
                            )
                        )
                    } else null
                ),
                QuoteBundleFragment.Inception(
                    correspondingQuoteId = "ea656f5f-40b2-4953-85d9-752b33e69e37",
                    startDate = if (startDateFromPreviousInsurer) null else LocalDate.now().plusDays(3),
                    currentInsurer = if (withCurrentInsurer) {
                        QuoteBundleFragment.CurrentInsurer2(
                            fragments = QuoteBundleFragment.CurrentInsurer2.Fragments(
                                CurrentInsurerFragment(
                                    id = "currentinsurerid2",
                                    displayName = "Test current insurer 2",
                                    switchable = true
                                )
                            )
                        )
                    } else null
                ),
                QuoteBundleFragment.Inception(
                    correspondingQuoteId = "ea656f5f-40b2-4953-85d9-752b33e69e36",
                    startDate = if (startDateFromPreviousInsurer) null else LocalDate.now().plusDays(5),
                    currentInsurer = if (withCurrentInsurer) {
                        QuoteBundleFragment.CurrentInsurer2(
                            fragments = QuoteBundleFragment.CurrentInsurer2.Fragments(
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
