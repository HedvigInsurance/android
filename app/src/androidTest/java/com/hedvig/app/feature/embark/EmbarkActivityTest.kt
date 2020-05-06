package com.hedvig.app.feature.embark

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import junit.framework.Assert.assertTrue
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmbarkActivityTest {
    @get:Rule
    val activityRule = ActivityTestRule(EmbarkActivity::class.java, false, false)

    @Test
    fun showsSpinnerWhileLoading() {
        activityRule.launchActivity(INTENT_WITH_STORY_NAME)
        onScreen<EmbarkScreen> {
            spinner {
                isVisible()
            }
        }
    }

    @Test
    fun endsActivityIfNoStoryNameIsProvided() {
        activityRule.launchActivity(Intent())
        assertTrue(activityRule.activity.isFinishing)
        assertTrue(activityRule.activityResult.resultCode == Activity.RESULT_CANCELED)
    }

    @Test
    fun showsFirstPassageWhenNetworkHasLoaded() {
        val webServer = MockWebServer()
        webServer.start(8080)

        webServer.enqueue(MockResponse().setBody("""{"data":{"embarkStory":{"__typename":"EmbarkStory","startPassage":"1","passages":[{"__typename":"EmbarkPassage","name":"TestPassage","id":"1","messages":[{"__typename":"EmbarkMessage","text":"test message"},{"__typename":"EmbarkMessage","text":"123"}],"action":{"__typename":"EmbarkSelectAction","data":{"__typename":"EmbarkSelectActionData","options":[{"__typename":"EmbarkSelectActionOption","link":{"__typename":"EmbarkLink","name":"TestPassage","label":"Test select action"}}]}}}]}}}"""))

        activityRule.launchActivity(INTENT_WITH_STORY_NAME)

        onScreen<EmbarkScreen> {
            messages {
                firstChild<MessageRow> {
                    text {
                        hasText("test message")
                    }
                }
                childAt<MessageRow>(1) {
                    text {
                        hasText("123")
                    }
                }
            }
            selectActions {
                firstChild<SelectAction> {
                    text {
                        hasText("Test select action")
                    }
                }
            }
        }
    }

    companion object {
        private val INTENT_WITH_STORY_NAME = Intent().apply {
            putExtra(EmbarkActivity.STORY_NAME, "example")
        }
    }

    class EmbarkScreen : Screen<EmbarkScreen>() {
        val spinner = KView { withId(R.id.loadingSpinner) }
        val messages = KRecyclerView({ withId(R.id.messages) }, { itemType(::MessageRow) })

        val selectActions = KRecyclerView({ withId(R.id.actions) }, { itemType(::SelectAction) })
    }

    class MessageRow(parent: Matcher<View>) : KRecyclerItem<MessageRow>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    class SelectAction(parent: Matcher<View>) : KRecyclerItem<SelectAction>(parent) {
        val text = KTextView { withMatcher(parent) }
    }
}
