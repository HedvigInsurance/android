package com.hedvig.android.notification.core

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.initializable.Initializable
import com.hedvig.android.language.LanguageService
import com.hedvig.android.language.withAppLanguage
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

/**
 * Renames the notification channels that already exist whenever the app language changes, including a change made
 * while the app was not running, so Android's notification settings show them in the app language. Channels are
 * otherwise only renamed when a notification is sent through them.
 */
@ContributesIntoSet(AppScope::class)
@Inject
internal class NotificationChannelLanguageUpdater(
  private val context: Context,
  private val languageService: LanguageService,
  private val applicationScope: ApplicationScope,
) : Initializable {
  override fun initialize() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val notificationManager = context.getSystemService<NotificationManager>() ?: return
    applicationScope.launch {
      languageService.language.collect { language ->
        val appLanguageContext = context.withAppLanguage(language)
        for (channel in allHedvigNotificationChannels) {
          if (notificationManager.getNotificationChannel(channel.channelId) != null) {
            channel.createChannel(appLanguageContext)
          }
        }
      }
    }
  }
}
