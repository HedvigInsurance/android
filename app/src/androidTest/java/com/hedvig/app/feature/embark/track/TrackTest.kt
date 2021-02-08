package com.hedvig.app.feature.embark.track

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.embarkTrackerModule
import com.hedvig.app.feature.embark.EmbarkTracker
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TRACK
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module

class TrackTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_TRACK) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    private val tracker = mockk<EmbarkTracker>(relaxed = true)

    @get:Rule
    val mockModuleRule = KoinMockModuleRule(
        listOf(embarkTrackerModule),
        listOf(module { single { tracker } })
    )

    @Test
    fun shouldTrackWhenEnteringPassage() = run {
        activityRule.launch(EmbarkActivity.newInstance(context(), this.javaClass.name))

        verify(exactly = 1) { tracker.track("Enter Passage") }
    }
}
