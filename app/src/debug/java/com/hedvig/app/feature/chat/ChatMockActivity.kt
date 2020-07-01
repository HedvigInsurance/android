package com.hedvig.app.feature.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.GenericDevelopmentAdapter
import com.hedvig.app.R
import com.hedvig.app.service.push.managers.ChatNotificationManager
import kotlinx.android.synthetic.debug.activity_generic_development.*

class ChatMockActivity : AppCompatActivity(R.layout.activity_generic_development) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root.adapter = GenericDevelopmentAdapter(
            listOf(
                GenericDevelopmentAdapter.Item.Header("Notifications"),
                GenericDevelopmentAdapter.Item.ClickableItem("Send chat message received-notification") {
                    ChatNotificationManager
                        .sendChatNotification(this)
                }
            )
        )
    }
}
