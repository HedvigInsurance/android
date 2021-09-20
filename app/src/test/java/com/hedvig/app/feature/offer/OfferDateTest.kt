package com.hedvig.app.feature.offer

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.hedvig.android.owldroid.fragment.CurrentInsurerFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.offer.ui.OfferStartDate
import com.hedvig.app.feature.offer.ui.changestartdate.getStartDate
import com.hedvig.app.testdata.feature.offer.builders.ConcurrentInceptionBuilder
import java.time.LocalDate
import org.junit.Test

class OfferDateTest {

    @Test
    fun `should show todays date for new offers`() {
        val concurrentInception = ConcurrentInceptionBuilder(
            startDate = null,
            currentInsurer = null
        ).build()

        val independentInception = OfferQuery.Inception1(
            asIndependentInceptions = OfferQuery.AsIndependentInceptions(
                inceptions = listOf(
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
                    )
                )
            ),
            asConcurrentInception = null
        )

        assertThat(concurrentInception.getStartDate()).isDataClassEqualTo(OfferStartDate.AtDate(LocalDate.now()))
        assertThat(independentInception.getStartDate()).isDataClassEqualTo(OfferStartDate.AtDate(LocalDate.now()))
    }

    @Test
    fun `should show todays date if current insurer is not switchable`() {
        val inception = ConcurrentInceptionBuilder(
            startDate = null,
            currentInsurer = OfferQuery.CurrentInsurer1(
                fragments = OfferQuery.CurrentInsurer1.Fragments(
                    CurrentInsurerFragment(
                        id = "testId",
                        displayName = "Test insurer",
                        switchable = false
                    )
                )
            )
        ).build()

        assertThat(inception.getStartDate()).isEqualTo(OfferStartDate.AtDate(LocalDate.now()))
    }

    @Test
    fun `should show switchable text if current insurer is switchable`() {
        val inception = ConcurrentInceptionBuilder(
            startDate = null,
            currentInsurer = OfferQuery.CurrentInsurer1(
                fragments = OfferQuery.CurrentInsurer1.Fragments(
                    CurrentInsurerFragment(
                        id = "testId",
                        displayName = "Test insurer",
                        switchable = true
                    )
                )
            )
        ).build()

        assertThat(inception.getStartDate()).isEqualTo(OfferStartDate.WhenCurrentPlanExpires)
    }

    @Test
    fun `should show multiple date text if independent inceptions have different start date`() {
        val inception = OfferQuery.Inception1(
            asIndependentInceptions = OfferQuery.AsIndependentInceptions(
                inceptions = listOf(
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
                                    switchable = false
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

            ),
            asConcurrentInception = null,
        )
        assertThat(inception.getStartDate()).isEqualTo(OfferStartDate.Multiple)
    }

    @Test
    fun `should show multiple date text if independent inceptions have at least one non switchable`() {
        val inception = OfferQuery.Inception1(
            asIndependentInceptions = OfferQuery.AsIndependentInceptions(
                inceptions = listOf(
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

            ),
            asConcurrentInception = null,
        )
        assertThat(inception.getStartDate()).isEqualTo(OfferStartDate.Multiple)
    }

    @Test
    fun `should show switchable date text if independent inceptions have all switchable`() {
        val inception = OfferQuery.Inception1(
            asIndependentInceptions = OfferQuery.AsIndependentInceptions(
                inceptions = listOf(
                    OfferQuery.Inception(
                        correspondingQuote = OfferQuery.CorrespondingQuote1(
                            asCompleteQuote1 = OfferQuery.AsCompleteQuote1(
                                displayName = "Test Insurance",
                                id = "ea656f5f-40b2-4953-85d9-752b33e69e38"
                            )
                        ),
                        startDate = null,
                        currentInsurer = OfferQuery.CurrentInsurer2(
                            fragments = OfferQuery.CurrentInsurer2.Fragments(
                                CurrentInsurerFragment(
                                    id = "currentinsurerid",
                                    displayName = "Test current insurer",
                                    switchable = true
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
                        startDate = null,
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
                        startDate = null,
                        currentInsurer = OfferQuery.CurrentInsurer2(
                            fragments = OfferQuery.CurrentInsurer2.Fragments(
                                CurrentInsurerFragment(
                                    id = "currentinsurerid3",
                                    displayName = "Test current insurer 3",
                                    switchable = true
                                )
                            )
                        )
                    )
                )

            ),
            asConcurrentInception = null,
        )
        assertThat(inception.getStartDate()).isEqualTo(OfferStartDate.WhenCurrentPlanExpires)
    }
}
