package com.hedvig.app.feature.embark

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.MoreOptionsQuery
import com.hedvig.app.feature.embark.screens.MoreOptionsScreen
import com.hedvig.app.feature.embark.ui.MoreOptionsActivity
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
    val activityRule = ActivityTestRule(MoreOptionsActivity::class.java, false, false)

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
        activityRule.launchActivity(MoreOptionsActivity.newInstance(context()))
        onScreen<MoreOptionsScreen> {
            recycler {
                childAt<MoreOptionsScreen.Row>(1) {
                    info {
                        click()
                        hasText("1234567890")
                    }
                }
            }
        }
    }
}
