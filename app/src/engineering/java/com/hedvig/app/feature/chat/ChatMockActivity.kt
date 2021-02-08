package com.hedvig.app.feature.chat

import androidx.core.os.bundleOf
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.MockActivity
import com.hedvig.app.feature.chat.service.ChatNotificationManager
import com.hedvig.app.feature.chat.service.ChatNotificationManager.DATA_NEW_MESSAGE_BODY
import com.hedvig.app.genericDevelopmentAdapter
import org.koin.core.module.Module

class ChatMockActivity : MockActivity() {
    override val original = emptyList<Module>()
    override val mocks = emptyList<Module>()

    override fun adapter() = genericDevelopmentAdapter {
        header("Notifications")
        clickableItem("Send chat message received-notification") {
            ChatNotificationManager
                .sendChatNotification(
                    this@ChatMockActivity,
                    RemoteMessage(
                        bundleOf(
                            DATA_NEW_MESSAGE_BODY to "Hello, world!"
                        )
                    )
                )
        }
    }
}
