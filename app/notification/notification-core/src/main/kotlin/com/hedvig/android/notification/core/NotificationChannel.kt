package com.hedvig.android.notification.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import hedvig.resources.R

sealed interface HedvigNotificationChannel {
  val channelId: String

  fun createChannel(context: Context)

  data object Chat : HedvigNotificationChannel {
    override fun createChannel(context: Context) {
      setupNotificationChannel(
        context,
        channelId,
        context.resources.getString(R.string.NOTIFICATION_CHAT_CHANNEL_NAME),
        context.resources.getString(R.string.NOTIFICATION_CHAT_CHANNEL_DESCRIPTION),
      )
    }

    override val channelId: String = "hedvig-chat"
  }

  data object CrossSell : HedvigNotificationChannel {
    override fun createChannel(context: Context) {
      setupNotificationChannel(
        context,
        channelId,
        context.resources.getString(hedvig.resources.R.string.PAYMENTS__CAMPAIGNS),
        context.resources.getString(hedvig.resources.R.string.NOTIFICATION_CHANNEL_CROSS_SELL_TITLE),
      )
    }

    override val channelId: String = "hedvig-cross-sell"
  }

  data object Other : HedvigNotificationChannel {
    override fun createChannel(context: Context) {
      setupNotificationChannel(
        context,
        channelId,
        context.resources.getString(R.string.NOTIFICATION_CHANNEL_GENERIC_TITLE),
      )
    }

    override val channelId: String = "hedvig-generic"
  }

  data object Payments : HedvigNotificationChannel {
    override fun createChannel(context: Context) {
      setupNotificationChannel(
        context,
        channelId,
        context.resources.getString(hedvig.resources.R.string.NOTIFICATION_CHANNEL_PAYMENT_TITLE),
        context.resources.getString(hedvig.resources.R.string.NOTIFICATION_CHANNEL_PAYMENT_DESCRIPTION),
      )
    }

    override val channelId: String = "hedvig-payments"
  }

  data object Referrals : HedvigNotificationChannel {
    override fun createChannel(context: Context) {
      setupNotificationChannel(
        context,
        channelId,
        context.resources.getString(R.string.NOTIFICATION_REFERRAL_CHANNEL_NAME),
        context.resources.getString(R.string.NOTIFICATION_REFERRAL_CHANNEL_DESCRIPTION),
      )
    }

    override val channelId: String = "hedvig-referral"
  }
}

private fun setupNotificationChannel(
  context: Context,
  channelId: String,
  channelName: String,
  channelDescription: String? = null,
) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val notificationManager = context.getSystemService<NotificationManager>() ?: return
    notificationManager.createNotificationChannel(
      NotificationChannel(
        channelId,
        channelName,
        NotificationManager.IMPORTANCE_HIGH,
      ).apply {
        channelDescription?.let { description = it }
      },
    )
  }
}
