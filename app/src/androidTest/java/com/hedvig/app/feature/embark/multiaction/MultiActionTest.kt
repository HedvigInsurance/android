package com.hedvig.app.feature.embark.multiaction

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_MULTI_ACTION
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class MultiActionTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_MULTI_ACTION) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun addBuildingButtonShouldShowBottomSheet() = run {
        activityRule.launch(EmbarkActivity.newInstance(context(), this.javaClass.name))

        MultiActionScreen {
            multiActionList {
                childAt<MultiActionScreen.AddBuildingButton>(0) {
                    click()
                }
            }
        }

        AddBuildingBottomSheetScreen {
            dropDownMenu {
                isVisible()
            }
        }
    }
}
