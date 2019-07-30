package com.hedvig.app.feature.marketing.service

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class MarketingTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun viewedStory(storyIndex: Int) = firebaseAnalytics.logEvent(
        "viewed_story",
        Bundle().apply {
            putInt("story_number", storyIndex + 1)
        }
    )

    fun loginClick(storyIndex: Int?, blurActive: Boolean?) = firebaseAnalytics.logEvent(
        "click_login",
        Bundle().apply {
            storyIndex?.let { putInt("story_number", it + 1) }
            putBoolean("final_screen_active", blurActive ?: false)
        }
    )

    fun getHedvigClick(storyIndex: Int?, blurActive: Boolean?) = firebaseAnalytics.logEvent(
        "click_get_hedvig",
        Bundle().apply {
            storyIndex?.let { putInt("story_number", it + 1) }
            putBoolean("final_screen_active", blurActive ?: false)
        }
    )

    fun dismissBlurOverlay() = firebaseAnalytics.logEvent("dismiss_blur_overlay", null)

    fun pause(storyIndex: Int?) = firebaseAnalytics.logEvent(
        "click_pause_story",
        Bundle().apply {
            storyIndex?.let { putInt("story_number", it + 1) }
        }
    )

    fun nextScreen(storyIndex: Int?) = firebaseAnalytics.logEvent(
        "click_next_screen",
        Bundle().apply {
            storyIndex?.let { putInt("story_number", it + 1) }
        }
    )

    fun previousSreen(storyIndex: Int?) = firebaseAnalytics.logEvent(
        "click_prev_screen",
        Bundle().apply {
            storyIndex?.let { putInt("story_number", it + 1) }
        }
    )
}
