package com.hedvig.app.feature.chat

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class ChatTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun closeChat() = firebaseAnalytics.logEvent("CHAT_CLOSE", null)
    fun restartChat() = firebaseAnalytics.logEvent("CHAT_RESTART", null)
    fun editMessage() = firebaseAnalytics.logEvent("CHAT_EDIT_MESSAGE", null)
    fun openUploadedFile() = firebaseAnalytics.logEvent("chat_open_file_upload", null)
    fun sendChatMessage() = firebaseAnalytics.logEvent("chat_click_send_message", null)
    fun openAttachFile() = firebaseAnalytics.logEvent("CHAT_UPLOAD_FILE", null)
    fun singleSelect(label: String) = firebaseAnalytics.logEvent("chat_single_select", Bundle().apply {
        putString("label", label)
    })
}
