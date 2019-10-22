package com.hedvig.app.feature.language

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class LanguageSelectionTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun selectLanguage(choice: String) =
        firebaseAnalytics.logEvent("select_language", Bundle().apply {
            putString("choice", choice)
        })
}
