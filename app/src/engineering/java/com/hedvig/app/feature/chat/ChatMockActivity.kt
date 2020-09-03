package com.hedvig.app.feature.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.GenericDevelopmentAdapter
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityGenericDevelopmentBinding
import com.hedvig.app.feature.chat.service.ChatNotificationManager
import com.hedvig.app.feature.chat.service.ChatNotificationManager.DATA_NEW_MESSAGE_BODY
import com.hedvig.app.util.extensions.viewBinding

class ChatMockActivity : AppCompatActivity(R.layout.activity_generic_development) {
    private val binding by viewBinding(ActivityGenericDevelopmentBinding::bind)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.root.adapter = GenericDevelopmentAdapter(
            listOf(
                GenericDevelopmentAdapter.Item.Header("Notifications"),
                GenericDevelopmentAdapter.Item.ClickableItem("Send chat message received-notification") {
                    ChatNotificationManager
                        .sendChatNotification(
                            this, RemoteMessage(
                                bundleOf(
                                    DATA_NEW_MESSAGE_BODY to "Hello, world!"
                                )
                            )
                        )
                }
            )
        )
    }
}
