package com.hedvig.app.feature.offer

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.embark.masking.ISO_8601_DATE
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.BUNDLE_WITH_CONCURRENT_INCEPTION_DATES_SPECIFIC_DATE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasNrOfChildren
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

class ConcurrentInceptionTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(OfferActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        OfferQuery.QUERY_DOCUMENT to apolloResponse { success(BUNDLE_WITH_CONCURRENT_INCEPTION_DATES_SPECIFIC_DATE) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldSetDateLabel() = run {
        activityRule.launch(OfferActivity.newInstance(context(), listOf("123")))
        onScreen<OfferScreen> {
            scroll {
                childAt<OfferScreen.HeaderItem>(0) {
                    startDate {
                        hasText(LocalDate.of(2021, 6, 22).format(ISO_8601_DATE))
                    }
                }
            }
        }
    }

    @Test
    fun shouldHaveOneDateView() = run {
        activityRule.launch(OfferActivity.newInstance(context(), listOf("123")))
        onScreen<OfferScreen> {
            scroll {
                childAt<OfferScreen.HeaderItem>(0) {
                    startDate {
                        click()
                    }
                }
            }
        }
        onScreen<ChangeDateBottomSheetScreen> {
            changeDateContainer {
                hasNrOfChildren(1)
            }
        }
    }
}
