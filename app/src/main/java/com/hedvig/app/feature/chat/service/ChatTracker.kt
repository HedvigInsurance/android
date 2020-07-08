package com.hedvig.app.feature.chat.service

import com.hedvig.app.util.jsonObjectOf
import com.mixpanel.android.mpmetrics.MixpanelAPI

class ChatTracker(
    private val mixpanel: MixpanelAPI
) {
    fun closeChat() = mixpanel.track("CHAT_CLOSE")
    fun restartChat() = mixpanel.track("CHAT_RESTART")
    fun editMessage() = mixpanel.track("CHAT_EDIT_MESSAGE_DESCRIPTION")
    fun openUploadedFile() = mixpanel.track("CHAT_FILE_UPLOADED")
    fun sendChatMessage() = mixpanel.track("chat_click_send_message")
    fun openAttachFile() = mixpanel.track("CHAT_UPLOAD_FILE")
    fun openSendGif() = mixpanel.track("GIF_BUTTON_TITLE")
    fun singleSelect(label: String) = mixpanel.track(
        "chat_single_select",
        jsonObjectOf("label" to label)
    )

    fun settings() = mixpanel.track("SETTINGS_ACCESSIBILITY_HINT")

    fun recordClaim() = mixpanel.track("AUDIO_INPUT_RECORD_DESCRIPTION")
    fun stopRecording() = mixpanel.track("AUDIO_INPUT_STOP_DESCRIPTION")
    fun redoClaim() = mixpanel.track("AUDIO_INPUT_REDO")
    fun playClaim() = mixpanel.track("AUDIO_INPUT_PLAY")
    fun uploadClaim() = mixpanel.track("AUDIO_INPUT_SAVE")
}
