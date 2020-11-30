package com.hedvig.app.feature.embark

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.MoreOptionsQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.screens.MoreOptionsScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.MORE_OPTIONS_DATA
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoreOptionsTest {

    @get:Rule
    val activityRule = ActivityTestRule(EmbarkActivity::class.java, false, false)

    var shouldFail = true

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        MoreOptionsQuery.QUERY_DOCUMENT to apolloResponse {
            if (shouldFail) {
                shouldFail = false
                graphQLError("error")
            } else {
                success(MORE_OPTIONS_DATA)
            }
        }
    )

    @Test
    fun openMoreOptionsActivity() {

        activityRule.launchActivity(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name
            )
        )
        onScreen<EmbarkScreen> {
            moreOptionsButton {
                click()
            }
        }
        onScreen<MoreOptionsScreen> {
            recycler {
                childAt<MoreOptionsScreen.Reload>(1) {
                    reload.click()
                }
                childAt<MoreOptionsScreen.Id>(1) {
                    id {
                        hasText("1234567890")
                    }
                }
            }
        }
    }
}
