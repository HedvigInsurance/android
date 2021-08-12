package com.hedvig.app.feature.chat.service

import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.app.util.jsonObjectOf

class ChatTracker(
    private val trackingFacade: TrackingFacade
) {
    fun closeChat() = trackingFacade.track("CHAT_CLOSE")
    fun restartChat() = trackingFacade.track("CHAT_RESTART")
    fun editMessage() = trackingFacade.track("CHAT_EDIT_MESSAGE_DESCRIPTION")
    fun openUploadedFile() = trackingFacade.track("CHAT_FILE_UPLOADED")
    fun sendChatMessage() = trackingFacade.track("chat_click_send_message")
    fun openAttachFile() = trackingFacade.track("CHAT_UPLOAD_FILE")
    fun openSendGif() = trackingFacade.track("GIF_BUTTON_TITLE")
    fun singleSelect(label: String) = trackingFacade.track(
        "chat_single_select",
        jsonObjectOf("label" to label)
    )

    fun settings() = trackingFacade.track("SETTINGS_ACCESSIBILITY_HINT")

    fun recordClaim() = trackingFacade.track("AUDIO_INPUT_RECORD_DESCRIPTION")
    fun stopRecording() = trackingFacade.track("AUDIO_INPUT_STOP_DESCRIPTION")
    fun redoClaim() = trackingFacade.track("AUDIO_INPUT_REDO")
    fun playClaim() = trackingFacade.track("AUDIO_INPUT_PLAY")
    fun uploadClaim() = trackingFacade.track("AUDIO_INPUT_SAVE")
}
