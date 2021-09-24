import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.ApolloMockServerRule
import com.hedvig.app.apolloResponse
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_FOR_STORE_VERSIONING
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class StoreVersionTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_FOR_STORE_VERSIONING) },
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldClearOneStoreVersionWhenNavigatingBack() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name,
                "",
            )
        )

        onScreen<EmbarkScreen> {
            step("Add an entry to embark store") {
                selectActions { firstChild<EmbarkScreen.SelectAction> { click() } }
            }
            step("Wait until next passage is showing") {
                messages {
                    firstChild<EmbarkScreen.MessageRow> {
                        text { hasText("a third message") }
                    }
                }
            }
            step("Go back") {
                pressBack()
            }
            step("Navigate forwards without adding entry to embark store") {
                selectActions { childAt<EmbarkScreen.SelectAction>(1) { click() } }
            }
            step("Verify that entry is no longer in store") {
                messages {
                    firstChild<EmbarkScreen.MessageRow> {
                        text { hasText("another test message") }
                    }
                }
            }
        }
    }
}
