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
 * The JVM has no platform light or dark mode to set, so this does nothing. The choice itself is stored by
 * [SettingsPresenter], for whatever renders the app to read.
 */
internal actual fun Theme.apply() {}
