package com.hedvig.app.feature.embark.api.graphqlmutation

import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.HELLO_MUTATION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GRAPHQL_MUTATION
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.seconds
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.Rule
import org.junit.Test

class NetworkErrorTest : TestCase() {
    @get:Rule
    val activityRule = ActivityTestRule(EmbarkActivity::class.java, false, false)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_GRAPHQL_MUTATION) },
        HELLO_MUTATION to apolloResponse {
            internalServerError()
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldRedirectWhenLoadingPassageWithGraphQLMutationApiThatIsError() = run {
        activityRule.launchActivity(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name
            )
        )

        onScreen<EmbarkScreen> {
            selectActions { firstChild<EmbarkScreen.SelectAction> { click() } }
            await atMost 2.seconds untilAsserted {
                messages {
                    hasSize(1)
                    firstChild<EmbarkScreen.MessageRow> {
                        text { hasText("a fourth message") }
                    }
                }
            }
        }
    }
}
