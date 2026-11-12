package com.hedvig.android.feature.profile.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.notification.permission.NotificationPermissionState
import com.hedvig.android.theme.Theme

/**
 * There are no system notification settings to open on the JVM, so the row is not shown.
 */
@Composable
internal actual fun NotificationSettingsRow(
  notificationPermissionState: NotificationPermissionState,
  modifier: Modifier,
) {
}

/**
 * The JVM has no platform light or dark mode to set. The app's own theme follows the stored choice.
 */
internal actual fun Theme.apply() {}
