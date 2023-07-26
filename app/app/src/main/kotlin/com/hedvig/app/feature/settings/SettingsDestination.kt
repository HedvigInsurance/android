package com.hedvig.app.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.card.HedvigBigCard
import com.hedvig.android.core.designsystem.component.card.HedvigInfoCard
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.dialog.SingleSelectDialog
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.market.Language
import com.hedvig.android.theme.Theme
import hedvig.resources.R

@Composable
internal fun SettingsDestination(
  viewModel: SettingsViewModel,
  onBackPressed: () -> Unit,
) {
  val context = LocalContext.current
  val notificationManager = remember(context) { NotificationManagerCompat.from(context) }
  var notificationsEnabled by remember { mutableStateOf(notificationManager.areNotificationsEnabled()) }
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner, notificationManager) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        notificationsEnabled = notificationManager.areNotificationsEnabled()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  SettingsScreen(
    uiState = uiState,
    onBackPressed = onBackPressed,
    notificationsEnabled = notificationsEnabled,
    onNotificationInfoDismissed = viewModel::dismissNotificationInfo,
    onLanguageSelected = viewModel::applyLanguage,
    onThemeSelected = viewModel::applyTheme,
  )
}

@Composable
fun SettingsScreen(
  uiState: SettingsViewModel.SettingsUiState,
  onBackPressed: () -> Unit,
  notificationsEnabled: Boolean,
  onNotificationInfoDismissed: () -> Unit,
  onLanguageSelected: (Language) -> Unit,
  onThemeSelected: (Theme) -> Unit,
) {
  val context = LocalContext.current
  HedvigScaffold(
    topAppBarText = stringResource(R.string.SETTINGS_TITLE),
    navigateUp = onBackPressed,
    modifier = Modifier.clearFocusOnTap(),
  ) {
    Column(Modifier.padding(16.dp)) {
      Spacer(Modifier.height(16.dp))
      LanguageWithDialog(
        languageOptions = uiState.languageOptions,
        selectedLanguage = uiState.selectedLanguage,
        selectLanguage = onLanguageSelected,
        enabled = true,
      )
      Spacer(Modifier.height(4.dp))
      ThemeWithDialog(
        themeOptions = uiState.themeOptions,
        selectedTheme = uiState.selectedTheme,
        selectTheme = onThemeSelected,
        enabled = true,
      )
      Spacer(Modifier.height(4.dp))
      HedvigBigCard(
        onClick = { startAndroidSettingsActivity(context) },
        inputText = if (notificationsEnabled) {
          stringResource(id = R.string.PROFILE_NOTIFICATIONS_STATUS_ON)
        } else {
          stringResource(id = R.string.PROFILE_NOTIFICATIONS_STATUS_OFF)
        },
        hintText = stringResource(id = R.string.SETTINGS_NOTIFICATIONS_TITLE),
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))

      AnimatedVisibility(
        visible = !notificationsEnabled && uiState.showNotificationInfo,
        enter = fadeIn(),
        exit = fadeOut(),
      ) {
        HedvigInfoCard(
          contentPadding = PaddingValues(12.dp),
        ) {
          Column {
            Row {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "info",
                modifier = Modifier
                  .padding(top = 2.dp)
                  .size(16.dp)
                  .padding(1.dp),
                tint = MaterialTheme.colorScheme.infoElement,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = stringResource(id = R.string.PROFILE_ALLOW_NOTIFICATIONS_INFO_LABEL),
                style = MaterialTheme.typography.bodyMedium,
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            ) {
              HedvigContainedSmallButton(
                text = stringResource(id = R.string.PUSH_NOTIFICATIONS_ALERT_ACTION_NOT_NOW),
                onClick = onNotificationInfoDismissed,
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.onPrimary,
                  contentColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.fillMaxWidth(0.5f),
              )
              Spacer(modifier = Modifier.width(12.dp))
              HedvigContainedSmallButton(
                text = stringResource(id = R.string.PUSH_NOTIFICATIONS_ALERT_ACTION_OK),
                onClick = { startAndroidSettingsActivity(context) },
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.onPrimary,
                  contentColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.fillMaxWidth(1f),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
@HedvigPreview
fun PreviewSettingsScreen() {
  HedvigTheme(useNewColorScheme = true) {
    Surface {
      SettingsScreen(
        onBackPressed = {},
        notificationsEnabled = false,
        onNotificationInfoDismissed = {},
        onLanguageSelected = {},
        onThemeSelected = {},
        uiState = SettingsViewModel.SettingsUiState(
          selectedLanguage = Language.SV_SE,
          languageOptions = listOf(Language.SV_SE, Language.EN_SE),
          selectedTheme = Theme.LIGHT,
          themeOptions = listOf(Theme.LIGHT),
          notificationOn = true,
          showNotificationInfo = true,
        ),
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
) {
  val context = LocalContext.current
  var showLanguagePickerDialog by rememberSaveable { mutableStateOf(false) }
  if (showLanguagePickerDialog) {
    SingleSelectDialog(
      title = stringResource(R.string.language_picker_modal_title),
      optionsList = languageOptions,
      onSelected = selectLanguage,
      getDisplayText = { context.getString(it.getLabel()) },
      getIsSelected = { selectedLanguage == it },
      getId = { it.name },
      onDismissRequest = { showLanguagePickerDialog = false },
    )
  }

  HedvigBigCard(
    onClick = { showLanguagePickerDialog = true },
    hintText = stringResource(id = R.string.language_picker_modal_title),
    inputText = context.getString(selectedLanguage.getLabel()),
    enabled = enabled,
    modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
internal fun ThemeWithDialog(
  themeOptions: List<Theme>,
  selectedTheme: Theme,
  selectTheme: (Theme) -> Unit,
  enabled: Boolean,
) {
  val context = LocalContext.current
  var showThemePickerDialog by rememberSaveable { mutableStateOf(false) }
  if (showThemePickerDialog) {
    SingleSelectDialog(
      title = stringResource(R.string.SETTINGS_THEME_TITLE),
      optionsList = themeOptions,
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
    inputText = stringResource(id = selectedTheme.getLabel()),
    enabled = enabled,
    modifier = Modifier.fillMaxWidth(),
  )
}

private fun startAndroidSettingsActivity(context: Context) {
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

fun Theme.getLabel() = when (this) {
  Theme.LIGHT -> R.string.SETTINGS_THEME_LIGHT
  Theme.DARK -> R.string.SETTINGS_THEME_DARK
  Theme.SYSTEM_DEFAULT -> R.string.SETTINGS_THEME_SYSTEM_DEFAULT
}
