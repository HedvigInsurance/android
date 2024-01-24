package com.hedvig.android.feature.profile.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.isGranted
import com.hedvig.android.core.designsystem.component.card.HedvigBigCard
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedLoadingIndicatorDebounced
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.dialog.SingleSelectDialog
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.language.Language
import com.hedvig.android.memberreminders.ui.ReminderCardEnableNotifications
import com.hedvig.android.notification.permission.NotificationPermissionDialog
import com.hedvig.android.notification.permission.rememberNotificationPermissionState
import com.hedvig.android.theme.Theme
import hedvig.resources.R

@Composable
internal fun SettingsDestination(viewModel: SettingsViewModel, openAppSettings: () -> Unit, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  SettingsScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    openAppSettings = openAppSettings,
    onNotificationInfoDismissed = { viewModel.emit(SettingsEvent.SnoozeNotificationPermissionReminder) },
    onLanguageSelected = { viewModel.emit(SettingsEvent.ChangeLanguage(it)) },
    onThemeSelected = { viewModel.emit(SettingsEvent.ChangeTheme(it)) },
  )
}

@Composable
private fun SettingsScreen(
  uiState: SettingsUiState,
  navigateUp: () -> Unit,
  openAppSettings: () -> Unit,
  onNotificationInfoDismissed: () -> Unit,
  onLanguageSelected: (Language) -> Unit,
  onThemeSelected: (Theme) -> Unit,
) {
  LaunchedEffect(uiState.selectedTheme) {
    uiState.selectedTheme?.apply()
  }
  val context = LocalContext.current
  HedvigScaffold(
    topAppBarText = stringResource(R.string.SETTINGS_TITLE),
    navigateUp = navigateUp,
    modifier = Modifier.clearFocusOnTap(),
  ) {
    when (uiState) {
      is SettingsUiState.Loading -> {
        HedvigFullScreenCenterAlignedLoadingIndicatorDebounced()
      }
      is SettingsUiState.Loaded -> {
        Spacer(Modifier.height(8.dp))
        LanguageWithDialog(
          languageOptions = uiState.languageOptions,
          selectedLanguage = uiState.selectedLanguage,
          selectLanguage = onLanguageSelected,
          enabled = true,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(4.dp))
        ThemeWithDialog(
          selectedTheme = uiState.selectedTheme,
          selectTheme = onThemeSelected,
          enabled = true,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(4.dp))
        val notificationPermissionState = rememberNotificationPermissionState()
        NotificationPermissionDialog(
          notificationPermissionState = notificationPermissionState,
          openAppSettings = openAppSettings,
        )
        HedvigBigCard(
          onClick = { startAndroidNotificationSettingsActivity(context) },
          inputText = if (notificationPermissionState.status.isGranted) {
            stringResource(id = R.string.PROFILE_NOTIFICATIONS_STATUS_ON)
          } else {
            stringResource(id = R.string.PROFILE_NOTIFICATIONS_STATUS_OFF)
          },
          hintText = stringResource(id = R.string.SETTINGS_NOTIFICATIONS_TITLE),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))

        AnimatedVisibility(
          visible = uiState.showNotificationReminder && !notificationPermissionState.status.isGranted,
          enter = fadeIn(),
          exit = fadeOut(),
        ) {
          Column {
            ReminderCardEnableNotifications(
              snoozeNotificationPermissionReminder = onNotificationInfoDismissed,
              requestNotificationPermission = notificationPermissionState::launchPermissionRequest,
              modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(16.dp))
          }
        }
      }
    }
  }
}

@Composable
@HedvigPreview
fun PreviewSettingsScreen() {
  HedvigTheme {
    Surface {
      SettingsScreen(
        uiState = SettingsUiState.Loaded(
          selectedLanguage = Language.SV_SE,
          languageOptions = listOf(Language.SV_SE, Language.EN_SE),
          selectedTheme = Theme.SYSTEM_DEFAULT,
          showNotificationReminder = true,
        ),
        navigateUp = {},
        openAppSettings = {},
        onNotificationInfoDismissed = {},
        onLanguageSelected = {},
        onThemeSelected = {},
      )
    }
  }
}

@Composable
internal fun LanguageWithDialog(
  languageOptions: List<Language>,
  selectedLanguage: Language,
  selectLanguage: (Language) -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var showLanguagePickerDialog by rememberSaveable { mutableStateOf(false) }
  if (showLanguagePickerDialog) {
    SingleSelectDialog(
      title = stringResource(R.string.language_picker_modal_title),
      optionsList = languageOptions,
      onSelected = selectLanguage,
      getDisplayText = { context.getString(it.label) },
      getIsSelected = { selectedLanguage == it },
      getId = { it.name },
      onDismissRequest = { showLanguagePickerDialog = false },
    )
  }

  HedvigBigCard(
    onClick = { showLanguagePickerDialog = true },
    hintText = stringResource(id = R.string.language_picker_modal_title),
    inputText = context.getString(selectedLanguage.label),
    enabled = enabled,
    modifier = modifier,
  )
}

@Composable
internal fun ThemeWithDialog(
  selectedTheme: Theme?,
  selectTheme: (Theme) -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var showThemePickerDialog by rememberSaveable { mutableStateOf(false) }
  if (showThemePickerDialog) {
    SingleSelectDialog(
      title = stringResource(R.string.SETTINGS_THEME_TITLE),
      optionsList = Theme.entries,
      onSelected = selectTheme,
      getDisplayText = { context.getString(it.getLabel()) },
      getIsSelected = { selectedTheme == it },
      getId = { it.name },
      onDismissRequest = { showThemePickerDialog = false },
    )
  }

  HedvigBigCard(
    onClick = { showThemePickerDialog = true },
    hintText = stringResource(R.string.SETTINGS_THEME_TITLE),
    inputText = stringResource(selectedTheme.getLabel()),
    enabled = enabled,
    modifier = modifier,
  )
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

fun Theme?.getLabel() = when (this) {
  Theme.LIGHT -> R.string.SETTINGS_THEME_LIGHT
  Theme.DARK -> R.string.SETTINGS_THEME_DARK
  Theme.SYSTEM_DEFAULT -> R.string.SETTINGS_THEME_SYSTEM_DEFAULT
  null -> R.string.not_selected
}

private fun Theme.apply() = when (this) {
  Theme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
  Theme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
  Theme.SYSTEM_DEFAULT -> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    } else {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
    }
  }
}
