package com.hedvig.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.service.push.managers.ReferralsNotificationManager
import com.hedvig.app.service.push.managers.ReferralsNotificationManager.DATA_MESSAGE_REFERRED_SUCCESS_NAME
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.debug.activity_notifications.*

class NotificationsActivity : BaseActivity(R.layout.activity_notifications) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root.adapter = NotificationAdapter(
            listOf(
                NotificationItem(
                    "Referrals Enabled"
                ) { ReferralsNotificationManager.sendReferralsEnabledNotification(this) },
                NotificationItem(
                    "Referrals Success"
                ) {
                    ReferralsNotificationManager.sendReferralNotification(
                        this, RemoteMessage(
                            bundleOf(
                                DATA_MESSAGE_REFERRED_SUCCESS_NAME to "William"
                            )
                        )
                    )
                }
            )
        )
    }

    data class NotificationItem(
        val title: String,
        val send: () -> Unit
    )

    class NotificationAdapter(
        private val items: List<NotificationItem>
    ) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
        class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.notification_row, parent, false)
        ) {
            fun bind(item: NotificationItem) {
                (itemView as TextView).apply {
                    text = item.title
                    setHapticClickListener { item.send() }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)
        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }
    }
}
