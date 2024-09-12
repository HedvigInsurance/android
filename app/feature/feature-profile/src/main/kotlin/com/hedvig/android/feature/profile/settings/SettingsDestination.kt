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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.PermissionStatus.Granted
import com.google.accompanist.permissions.isGranted
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.HedvigBigCard
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDialog
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigRedTextButton
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.SingleSelectDialog
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.clearFocusOnTap
import com.hedvig.android.feature.profile.settings.SettingsUiState.Loaded
import com.hedvig.android.language.Language
import com.hedvig.android.language.Language.EN_SE
import com.hedvig.android.language.Language.SV_SE
import com.hedvig.android.memberreminders.ui.ReminderCardEnableNotifications
import com.hedvig.android.notification.permission.NotificationPermissionDialog
import com.hedvig.android.notification.permission.NotificationPermissionState
import com.hedvig.android.notification.permission.rememberNotificationPermissionState
import com.hedvig.android.theme.Theme
import com.hedvig.android.theme.Theme.SYSTEM_DEFAULT
import hedvig.resources.R

@Composable
internal fun SettingsDestination(
  viewModel: SettingsViewModel,
  navigateUp: () -> Unit,
  openAppSettings: () -> Unit,
  onNavigateToDeleteAccountFeature: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  SettingsScreen(
    uiState = uiState,
    notificationPermissionState = rememberNotificationPermissionState(),
    navigateUp = navigateUp,
    openAppSettings = openAppSettings,
    onNotificationInfoDismissed = { viewModel.emit(SettingsEvent.SnoozeNotificationPermissionReminder) },
    onLanguageSelected = { viewModel.emit(SettingsEvent.ChangeLanguage(it)) },
    onThemeSelected = { viewModel.emit(SettingsEvent.ChangeTheme(it)) },
    onTerminateAccountClicked = onNavigateToDeleteAccountFeature,
    changeSubscriptionPreference = {
      viewModel.emit(SettingsEvent.ChangeSubscriptionPreference(it))
    },
  )
}

@Composable
private fun SettingsScreen(
  uiState: SettingsUiState,
  notificationPermissionState: NotificationPermissionState,
  navigateUp: () -> Unit,
  changeSubscriptionPreference: (Boolean) -> Unit,
  openAppSettings: () -> Unit,
  onNotificationInfoDismissed: () -> Unit,
  onLanguageSelected: (Language) -> Unit,
  onThemeSelected: (Theme) -> Unit,
  onTerminateAccountClicked: () -> Unit,
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
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      is Loaded -> {
        Spacer(Modifier.height(8.dp))
        LanguageWithDialog(
          languageOptions = uiState.languageOptions,
          selectedLanguage = uiState.selectedLanguage,
          selectLanguage = onLanguageSelected,
          enabled = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(4.dp))
        ThemeWithDialog(
          selectedTheme = uiState.selectedTheme,
          selectTheme = onThemeSelected,
          enabled = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(4.dp))
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
          labelText = stringResource(id = R.string.SETTINGS_NOTIFICATIONS_TITLE),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(4.dp))
        if (uiState.showEmailSubscriptionPreferences) {
          EmailSubscriptionWithDialog(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            onConfirmUnsubscribeClick = { changeSubscriptionPreference(false) },
            onSubscribeClick = { changeSubscriptionPreference(true) },
            isSubscribedToEmails = uiState.isSubscribedToEmails ?: true,
            enabled = true,
            hasError = uiState.emailSubscriptionPreferenceError,
          )
          Spacer(Modifier.height(16.dp))
        }

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
        Spacer(Modifier.weight(1f))
        Row(
          Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
        ) {
          HedvigRedTextButton(
            text = stringResource(R.string.SETTINGS_SCREEN_DELETE_ACCOUNT_BUTTON),
            onClick = onTerminateAccountClicked,
            modifier = Modifier
              .padding(horizontal = 16.dp),
          )
        }
        Spacer(Modifier.height(16.dp))
      }
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
  var showLanguagePickerDialog by rememberSaveable { mutableStateOf(false) }
  if (showLanguagePickerDialog) {
    val entries = buildList {
      languageOptions.forEachIndexed { index, language ->
        add(
          RadioOptionData(
            id = index.toString(),
            optionText = stringResource(language.label),
            chosenState = if (selectedLanguage == language) Chosen else NotChosen,
          ),
        )
      }
    }
    SingleSelectDialog(
      title = stringResource(R.string.language_picker_modal_title),
      optionsList = entries,
      onSelected = {
        val index = it.id.toInt()
        val language = languageOptions[index]
        selectLanguage(language)
      },
      onDismissRequest = { showLanguagePickerDialog = false },
    )
  }

  HedvigBigCard(
    onClick = { showLanguagePickerDialog = true },
    labelText = stringResource(id = R.string.language_picker_modal_title),
    inputText = stringResource(selectedLanguage.label),
    enabled = enabled,
    modifier = modifier,
  )
}

@Composable
internal fun EmailSubscriptionWithDialog(
  onConfirmUnsubscribeClick: () -> Unit,
  onSubscribeClick: () -> Unit,
  isSubscribedToEmails: Boolean,
  enabled: Boolean,
  hasError: Boolean,
  modifier: Modifier = Modifier,
) {
  var showSubscriptionPrefDialog by rememberSaveable { mutableStateOf(false) }
  if (showSubscriptionPrefDialog) {
    HedvigDialog(
      style = DialogStyle.NoButtons,
      onDismissRequest = { showSubscriptionPrefDialog = false },
    ) {
      EmptyState(
        modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp),
        text = stringResource(R.string.SETTINGS_SCREEN_EMAIL_PREFERENCES),
        description = stringResource(R.string.SETTINGS_SCREEN_UNSUBSCRIBE_DESCRIPTION),
      )
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
      ) {
        HedvigTextButton(
          modifier = Modifier.weight(1f),
          text = stringResource(R.string.general_close_button),
          onClick = { showSubscriptionPrefDialog = false },
        )
        Spacer(Modifier.width(8.dp))
        HedvigButton(
          buttonSize = Medium,
          enabled = true,
          onClick = {
            onConfirmUnsubscribeClick()
            showSubscriptionPrefDialog = false
          },
          text = stringResource(R.string.SETTINGS_SCREEN_CONFIRM_UNSUBSCRIBE),
        )
      }
    }
  }
  Column {
    HedvigBigCard(
      onClick = {
        if (isSubscribedToEmails) {
          showSubscriptionPrefDialog = true
        } else {
          onSubscribeClick()
        }
      },
      labelText = stringResource(id = R.string.SETTINGS_SCREEN_EMAIL_PREFERENCES),
      inputText = if (isSubscribedToEmails) {
        stringResource(id = R.string.GENERAL_SUBSCRIBED)
      } else {
        stringResource(id = R.string.GENERAL_UNSUBSCRIBED)
      },
      enabled = enabled,
      modifier = modifier,
    )
    AnimatedVisibility(visible = hasError) {
      HedvigText(
        text = stringResource(id = R.string.something_went_wrong),
        style = HedvigTheme.typography.bodySmall,
        color = HedvigTheme.colorScheme.signalRedText,
        modifier = Modifier.padding(horizontal = 32.dp),
      )
    }
  }
}

@Composable
internal fun ThemeWithDialog(
  selectedTheme: Theme?,
  selectTheme: (Theme) -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  var showThemePickerDialog by rememberSaveable { mutableStateOf(false) }
  if (showThemePickerDialog) {
    val entries = buildList {
      Theme.entries.forEachIndexed { index, theme ->
        add(
          RadioOptionData(
            id = index.toString(),
            optionText = stringResource(theme.getLabel()),
            chosenState = if (selectedTheme == theme) Chosen else NotChosen,
          ),
        )
      }
    }
    SingleSelectDialog(
      title = stringResource(R.string.SETTINGS_THEME_TITLE),
      optionsList = entries,
      onSelected = {
        val index = it.id.toInt()
        val theme = Theme.entries[index] // todo: could be outOfBounds etc?
        selectTheme(theme)
      },
      onDismissRequest = { showThemePickerDialog = false },
    )
  }
  HedvigBigCard(
    onClick = { showThemePickerDialog = true },
    labelText = stringResource(R.string.SETTINGS_THEME_TITLE),
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

@Composable
@HedvigPreview
fun PreviewSettingsScreen() {
  HedvigTheme {
    Surface {
      SettingsScreen(
        uiState = Loaded(
          selectedLanguage = SV_SE,
          languageOptions = listOf(SV_SE, EN_SE),
          selectedTheme = SYSTEM_DEFAULT,
          showNotificationReminder = true,
          isSubscribedToEmails = true,
          showEmailSubscriptionPreferences = true,
          emailSubscriptionPreferenceError = true,
        ),
        notificationPermissionState = object : NotificationPermissionState {
          override val showDialog = false

          override fun dismissDialog() {}

          override fun launchPermissionRequest() {}

          override val permission: String = ""
          override val status: PermissionStatus = Granted
        },
        navigateUp = {},
        openAppSettings = {},
        onNotificationInfoDismissed = {},
        onLanguageSelected = {},
        onThemeSelected = {},
        onTerminateAccountClicked = {},
        changeSubscriptionPreference = {},
      )
    }
  }
}
