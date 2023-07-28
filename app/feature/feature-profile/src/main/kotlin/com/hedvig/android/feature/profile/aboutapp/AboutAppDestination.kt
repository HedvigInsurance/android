package com.hedvig.android.feature.profile.aboutapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import hedvig.resources.R

@Composable
internal fun AboutAppDestination(
  viewModel: AboutAppViewModel,
  onBackPressed: () -> Unit,
  showOpenSourceLicenses: () -> Unit,
  appVersionName: String,
  appVersionCode: String,
  isProduction: Boolean,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val memberId = uiState.memberId

  AboutAppScreen(
    memberId = memberId,
    appVersionName = appVersionName,
    appVersionCode = appVersionCode,
    onBackPressed = onBackPressed,
    showOpenSourceLicenses = showOpenSourceLicenses,
    isProduction = isProduction,
  )
}

@Composable
private fun AboutAppScreen(
  memberId: String?,
  appVersionName: String,
  appVersionCode: String,
  onBackPressed: () -> Unit,
  showOpenSourceLicenses: () -> Unit,
  isProduction: Boolean,
) {
  HedvigScaffold(
    topAppBarText = stringResource(R.string.PROFILE_ABOUT_APP_TITLE),
    navigateUp = onBackPressed,
    modifier = Modifier.clearFocusOnTap(),
  ) {
    Spacer(Modifier.height(16.dp))
    Column {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
      ) {
        Text(stringResource(id = R.string.PROFILE_ABOUT_APP_MEMBER_ID))
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
          Text(memberId ?: "")
        }
      }
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
      ) {
        Text(stringResource(id = R.string.PROFILE_ABOUT_APP_VERSION))
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
          Text(
            text = buildString {
              append(appVersionName)
              if (!isProduction) {
                append(" (")
                append(appVersionCode)
                append(")")
              }
            },
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = showOpenSourceLicenses)
          .padding(16.dp),
      ) {
        Text(stringResource(R.string.PROFILE_ABOUT_APP_LICENSE_ATTRIBUTIONS))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewAboutAppScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AboutAppScreen("123", "11.4.3", "42", {}, {}, false)
    }
  }
}
