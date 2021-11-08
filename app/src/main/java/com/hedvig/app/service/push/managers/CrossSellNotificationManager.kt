package com.hedvig.app.service.push.managers

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.R
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.crossselling.ui.CrossSellTracker
import com.hedvig.app.feature.crossselling.ui.detail.CrossSellDetailActivity
import com.hedvig.app.feature.crossselling.usecase.GetCrossSellsUseCase
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.service.push.DATA_MESSAGE_BODY
import com.hedvig.app.service.push.DATA_MESSAGE_TITLE
import com.hedvig.app.service.push.getImmutablePendingIntentFlags
import com.hedvig.app.service.push.setupNotificationChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CrossSellNotificationManager(
    private val crossSellsUseCase: GetCrossSellsUseCase,
    private val tracker: CrossSellTracker,
) {
    fun sendCrossSellNotification(context: Context, remoteMessage: RemoteMessage) {
        val title = remoteMessage.data[DATA_MESSAGE_TITLE]
        val body = remoteMessage.data[DATA_MESSAGE_BODY]
        val type = remoteMessage.data[CROSS_SELL_TYPE]

        CoroutineScope(Dispatchers.IO).launch {
            val crossSell = getCrossSell(type)
            tracker.notificationReceived(crossSell)

            val intent = if (crossSell != null) {
                createCrossSellIntent(context, crossSell)
            } else {
                createInsuranceTabIntent(context)
            }

            val notification = createNotification(
                context = context,
                title = title,
                body = body,
                pendingIntent = intent
            )
            notify(context, notification)
        }
    }

    private fun createCrossSellIntent(context: Context, crossSell: CrossSellData): PendingIntent? {
        val builder = TaskStackBuilder.create(context)
        val intent = CrossSellDetailActivity.newInstance(
            context = context,
            crossSell = crossSell,
            openedFromNotification = true,
        )
        builder.addNextIntentWithParentStack(intent)
        return builder.getPendingIntent(0, getImmutablePendingIntentFlags())
    }

    private fun createInsuranceTabIntent(context: Context): PendingIntent? {
        val builder = TaskStackBuilder.create(context)
        val intent = LoggedInActivity.newInstance(
            context = context,
            initialTab = LoggedInTabs.INSURANCE,
            withoutHistory = true
        )
        builder.addNextIntentWithParentStack(intent)
        return builder.getPendingIntent(0, getImmutablePendingIntentFlags())
    }

    private fun createNotification(
        context: Context,
        title: String?,
        body: String?,
        pendingIntent: PendingIntent?
    ): Notification {
        return NotificationCompat
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
    }

    private fun notify(context: Context, notification: Notification) {
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
