package com.hedvig.app.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.card.HedvigBigCard
import com.hedvig.android.core.designsystem.component.card.HedvigInfoCard
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.core.ui.dialog.SingleSelectDialog
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.MarketManager
import com.hedvig.app.authenticate.LogoutUseCase
import hedvig.resources.R

@Composable
internal fun SettingsDestination(
  marketManager: MarketManager,
  viewModel: SettingsViewModel,
  languageService: LanguageService,
  logoutUseCase: LogoutUseCase,
  onBackPressed: () -> Unit,
) {
  SettingsScreen(onBackPressed)
}

@Composable
fun SettingsScreen(
  onBackPressed: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(
      Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
    ) {
      TopAppBarWithBack(
        onClick = onBackPressed,
        title = stringResource(R.string.SETTINGS_TITLE),
        contentPadding = WindowInsets.safeDrawing
          .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
          .asPaddingValues(),
      )
      Column(Modifier.padding(16.dp)) {
        Spacer(Modifier.height(16.dp))
        LanguageWithDialog(
          languageOptions = listOf("Engelska", "Svenska"),
          selectedLanguage = "Svenska",
          selectLanguage = {},
          enabled = true,
        )
        Spacer(Modifier.height(4.dp))
        ThemeWithDialog(
          themeOptions = listOf("Light", "Dark", "System Default"),
          selectedTheme = "Light",
          selectTheme = {},
          enabled = true,
        )
        Spacer(Modifier.height(4.dp))
        HedvigBigCard(
          onClick = {},
          inputText = stringResource(id = R.string.PROFILE_NOTIFICATIONS_STATUS_ON),
          hintText = stringResource(id = R.string.SETTINGS_NOTIFICATIONS_TITLE),
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
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
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.onPrimary,
                  contentColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.fillMaxWidth(0.5f),
              )
              Spacer(modifier = Modifier.width(12.dp))
              HedvigContainedSmallButton(
                text = stringResource(id = R.string.PUSH_NOTIFICATIONS_ALERT_ACTION_OK),
                onClick = {},
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
      SettingsScreen({})
    }
  }
}

@Composable
internal fun LanguageWithDialog(
  languageOptions: List<String>,
  selectedLanguage: String?,
  selectLanguage: (String) -> Unit,
  enabled: Boolean,
) {
  var showLanguagePickerDialog by rememberSaveable { mutableStateOf(false) }
  if (showLanguagePickerDialog) {
    SingleSelectDialog(
      title = stringResource(R.string.language_picker_modal_title),
      optionsList = languageOptions,
      onSelected = selectLanguage,
      getDisplayText = { it },
      getIsSelected = { selectedLanguage == it },
      getId = { it },
      onDismissRequest = { showLanguagePickerDialog = false },
    )
  }

  HedvigBigCard(
    onClick = { showLanguagePickerDialog = true },
    hintText = stringResource(id = R.string.language_picker_modal_title),
    inputText = selectedLanguage,
    enabled = enabled,
    modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
internal fun ThemeWithDialog(
  themeOptions: List<String>,
  selectedTheme: String?,
  selectTheme: (String) -> Unit,
  enabled: Boolean,
) {
  var showThemePickerDialog by rememberSaveable { mutableStateOf(false) }
  if (showThemePickerDialog) {
    SingleSelectDialog(
      title = stringResource(R.string.SETTINGS_THEME_TITLE),
      optionsList = themeOptions,
      onSelected = selectTheme,
      getDisplayText = { it },
      getIsSelected = { selectedTheme == it },
      getId = { it },
      onDismissRequest = { showThemePickerDialog = false },
    )
  }

  HedvigBigCard(
    onClick = { showThemePickerDialog = true },
    hintText = stringResource(R.string.SETTINGS_THEME_TITLE),
    inputText = selectedTheme,
    enabled = enabled,
    modifier = Modifier.fillMaxWidth(),
  )
}
