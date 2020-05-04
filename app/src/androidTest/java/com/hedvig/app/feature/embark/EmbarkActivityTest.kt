package com.hedvig.app.feature.embark

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.agoda.kakao.text.KTextView
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.R
import junit.framework.Assert.assertTrue
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.KoinComponent
import org.koin.core.inject

@RunWith(AndroidJUnit4::class)
class EmbarkActivityTest : KoinComponent {

    private val apolloClientWrapper: ApolloClientWrapper by inject()

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

        webServer.enqueue(MockResponse().setBody("""{"data":{"embarkStory":{"__typename":"EmbarkStory","startPassage":"1","passages":[{"__typename":"EmbarkPassage","id":"1","messages":[{"__typename":"EmbarkMessage","text":"test message"}]}]}}}"""))

        activityRule.launchActivity(INTENT_WITH_STORY_NAME)

        onScreen<EmbarkScreen> {
            messages {
                hasText("test message")
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
        val messages = KTextView { withId(R.id.messages) }
    }
}
