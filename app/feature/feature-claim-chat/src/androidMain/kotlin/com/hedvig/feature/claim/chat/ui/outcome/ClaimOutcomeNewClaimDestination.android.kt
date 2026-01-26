package com.hedvig.feature.claim.chat.ui.outcome

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.android.notification.permission.NotificationPermissionState
import com.hedvig.android.notification.permission.rememberNotificationPermissionState
import hedvig.resources.CLAIMS_ACTIVATE_NOTIFICATIONS_BODY
import hedvig.resources.CLAIMS_ACTIVATE_NOTIFICATIONS_CTA
import hedvig.resources.CLAIM_CHAT_GET_NOTIFIED
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun NotificationPermissionSection(key: Unit) {
  val notificationPermissionState = rememberNotificationPermissionState()
  val context = LocalContext.current
  val bottomSheetState = rememberHedvigBottomSheetState<Unit>()
  LaunchedEffect(key) {
    if (!notificationPermissionState.status.isGranted) {
      bottomSheetState.show()
    }
  }
  ClaimChatNotificationPermissionBottomSheet(
    sheetState = bottomSheetState,
    notificationPermissionState = notificationPermissionState,
    openAppSettings = {
      startAndroidNotificationSettingsActivity(context)
    },
  )
}

@Composable
fun ClaimChatNotificationPermissionBottomSheet(
  sheetState: HedvigBottomSheetState<Unit>,
  notificationPermissionState: NotificationPermissionState,
  openAppSettings: () -> Unit,
) {
  HedvigBottomSheet(sheetState) {
    HedvigText(
      text = stringResource(Res.string.CLAIM_CHAT_GET_NOTIFIED),
      modifier = Modifier
        .fillMaxWidth()
        .semantics { heading() },
    )
    Spacer(Modifier.height(8.dp))
    HedvigText(
      text = stringResource(Res.string.CLAIMS_ACTIVATE_NOTIFICATIONS_BODY),
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    HedvigButton(
      text = stringResource(Res.string.CLAIMS_ACTIVATE_NOTIFICATIONS_CTA),
      onClick = {
        sheetState.dismiss()
        notificationPermissionState.dismissDialog()
        if (!notificationPermissionState.status.shouldShowRationale) {
          openAppSettings()
        } else {
          notificationPermissionState.launchPermissionRequest()
        }
      },
      modifier = Modifier.fillMaxWidth(),
      enabled = true,
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.general_cancel_button),
      onClick = {
        sheetState.dismiss()
        notificationPermissionState.dismissDialog()
      },
      modifier = Modifier.fillMaxWidth(),
      enabled = true,
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
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
