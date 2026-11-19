package com.hedvig.android.feature.profile.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigBigCard
import com.hedvig.android.notification.permission.NotificationPermissionState
import com.hedvig.android.notification.permission.isGranted
import com.hedvig.android.theme.Theme
import hedvig.resources.PROFILE_NOTIFICATIONS_STATUS_OFF
import hedvig.resources.PROFILE_NOTIFICATIONS_STATUS_ON
import hedvig.resources.Res
import hedvig.resources.SETTINGS_NOTIFICATIONS_TITLE
import org.jetbrains.compose.resources.stringResource

@Composable
internal actual fun NotificationSettingsRow(
  notificationPermissionState: NotificationPermissionState,
  modifier: Modifier,
) {
  val context = LocalContext.current
  HedvigBigCard(
    onClick = { startAndroidNotificationSettingsActivity(context) },
    inputText = if (notificationPermissionState.status.isGranted) {
      stringResource(Res.string.PROFILE_NOTIFICATIONS_STATUS_ON)
    } else {
      stringResource(Res.string.PROFILE_NOTIFICATIONS_STATUS_OFF)
    },
    labelText = stringResource(Res.string.SETTINGS_NOTIFICATIONS_TITLE),
    modifier = modifier,
  )
  Spacer(Modifier.height(4.dp))
}

private fun startAndroidNotificationSettingsActivity(context: Context) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    context.startActivity(
      Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
      },
    )
  } else {
    context.startActivity(
      Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        data = Uri.fromParts("package", context.packageName, null)
      },
    )
  }
}

internal actual fun Theme.apply() = when (this) {
  Theme.LIGHT -> {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
  }

  Theme.DARK -> {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
  }

  Theme.SYSTEM_DEFAULT -> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    } else {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
    }
  }
}
