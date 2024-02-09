package com.hedvig.android.feature.profile.aboutapp

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.dialog.HedvigAlertDialog
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import hedvig.resources.R

@Composable
internal fun AboutAppDestination(
  viewModel: AboutAppViewModel,
  onBackPressed: () -> Unit,
  showOpenSourceLicenses: () -> Unit,
  hedvigBuildConstants: HedvigBuildConstants,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val memberId = uiState.memberId

  AboutAppScreen(
    memberId = memberId,
    onBackPressed = onBackPressed,
    showOpenSourceLicenses = showOpenSourceLicenses,
    isProduction = hedvigBuildConstants.isProduction,
    appVersionName = hedvigBuildConstants.appVersionName,
    appVersionCode = hedvigBuildConstants.appVersionCode,
  )
}

@Composable
private fun AboutAppScreen(
  memberId: String?,
  onBackPressed: () -> Unit,
  showOpenSourceLicenses: () -> Unit,
  isProduction: Boolean,
  appVersionName: String,
  appVersionCode: String,
) {
  var showSubmitBugWarning by remember { mutableStateOf(false) }

  HedvigScaffold(
    topAppBarText = stringResource(R.string.PROFILE_ABOUT_APP_TITLE),
    navigateUp = onBackPressed,
    modifier = Modifier.clearFocusOnTap(),
  ) {
    val localContext = LocalContext.current

    if (showSubmitBugWarning) {
      HedvigAlertDialog(
        title = null,
        text = "You cannot send claims here. This is only to tell us about bugs in the app and other technical problems",
        // todo: remove hardcoded strings here for app_info.submit_warning.
        onDismissRequest = {
          showSubmitBugWarning = false
        },
        onConfirmClick = {
          openEmailClientWithPrefilledData(
            localContext,
            "mariia.panasetskaia@hedvig.com",
            "bug report",
            "there is a huge bug in your app!",
          ) // todo: remove hardcoded strings here
        },
        confirmButtonLabel = stringResource(id = R.string.app_info_submit_bug_continue),
        dismissButtonLabel = stringResource(id = R.string.app_info_submit_bug_go_back),
      )
    }

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
      Spacer(Modifier.height(16.dp))
      Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth(),
      ) {
        HedvigContainedSmallButton(
          text = "Submit bug", // todo: remove hardcoded strings here for app_info.submit_bug.button
          onClick = { showSubmitBugWarning = true },
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewAboutAppScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AboutAppScreen(
        memberId = "123",
        onBackPressed = {},
        showOpenSourceLicenses = {},
        isProduction = false,
        appVersionName = "11.3.2",
        appVersionCode = "43",
      )
    }
  }
}

private fun openEmailClientWithPrefilledData(
  context: Context,
  email: String,
  letterSubject: String,
  letterBody: String,
) {
  val sendLetterIntent: Intent = Intent().apply {
    action = Intent.ACTION_SENDTO
    data = Uri.parse("mailto:")
    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
    putExtra(Intent.EXTRA_SUBJECT, letterSubject)
    putExtra(Intent.EXTRA_TEXT, letterBody)
  }
  context.startActivity(
    Intent.createChooser(
      sendLetterIntent, "Send a report about a bug in Hedvig app",
    ),
  ) // todo: do we need to localise this title here? it would only show in chooser if the device has multiple email clients
}
