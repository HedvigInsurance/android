package com.hedvig.app.service.push.managers

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.R
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.crossselling.ui.detail.CrossSellDetailActivity
import com.hedvig.app.feature.crossselling.usecase.GetCrossSellsUseCase
import com.hedvig.app.service.push.DATA_MESSAGE_BODY
import com.hedvig.app.service.push.DATA_MESSAGE_TITLE
import com.hedvig.app.service.push.getImmutablePendingIntentFlags
import com.hedvig.app.service.push.setupNotificationChannel
import e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CrossSellNotificationManager(
    private val crossSellsUseCase: GetCrossSellsUseCase
) {
    fun sendCrossSellNotification(context: Context, remoteMessage: RemoteMessage) {
        val title = remoteMessage.data[DATA_MESSAGE_TITLE]
        val body = remoteMessage.data[DATA_MESSAGE_BODY]
        val type = remoteMessage.data[CROSS_SELL_TYPE]

        CoroutineScope(Dispatchers.IO).launch {
            val crossSell = getCrossSell(type)
            if (crossSell != null) {
                createIntentAndNotify(
                    context = context,
                    title = title,
                    body = body,
                    crossSell = crossSell
                )
            } else {
                e { "Could not find cross-sell with type: $type" }
            }
        }
    }

    private fun createIntentAndNotify(
        context: Context,
        title: String?,
        body: String?,
        crossSell: CrossSellData
    ) {
        val pendingIntent = TaskStackBuilder
            .create(context)
            .run {
                val intent = CrossSellDetailActivity.newInstance(context, crossSell)
                addNextIntentWithParentStack(intent)
                getPendingIntent(0, getImmutablePendingIntentFlags())
            }
        val notification = NotificationCompat
            .Builder(
                context,
                CROSS_SELL_CHANNEL_ID
            )
            .setSmallIcon(R.drawable.ic_hedvig_h)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setChannelId(CROSS_SELL_CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat
            .from(context)
            .notify(
                CROSS_SELL_NOTIFICATION_ID,
                notification
            )
    }

    fun createChannel(context: Context) {
        setupNotificationChannel(
            context,
            CROSS_SELL_CHANNEL_ID,
            context.resources.getString(R.string.NOTIFICATION_CHANNEL_CROSS_SELL_TITLE)
        )
    }

    private suspend fun getCrossSell(crossSellType: String?): CrossSellData? {
        return crossSellsUseCase.invoke().firstOrNull {
            it.crossSellType == crossSellType
        }
    }

    companion object {
        private const val CROSS_SELL_CHANNEL_ID = "hedvig-cross-sell"
        private const val CROSS_SELL_TYPE = "CROSS_SELL_TYPE"
        private const val CROSS_SELL_NOTIFICATION_ID = 11
    }
}
