package com.hedvig.android.feature.profile.aboutapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.clearFocusOnTap
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
  HedvigScaffold(
    topAppBarText = stringResource(R.string.PROFILE_ABOUT_APP_TITLE),
    navigateUp = onBackPressed,
    modifier = Modifier.clearFocusOnTap(),
  ) {
    var showSubmitBugWarning by remember { mutableStateOf(false) }
    if (showSubmitBugWarning) {
      SubmitBugWarningDialog(
        memberId,
        appVersionName,
      ) {
        showSubmitBugWarning = false
      }
    }
    Spacer(Modifier.height(16.dp))
    HorizontalItemsWithMaximumSpaceTaken(
      spaceBetween = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      startSlot = {
        HedvigText(stringResource(id = R.string.PROFILE_ABOUT_APP_MEMBER_ID))
      },
      endSlot = {
        HedvigText(
          memberId ?: "",
          color = HedvigTheme.colorScheme.textSecondary,
          textAlign = TextAlign.End,
        )
      },
    )
    HorizontalItemsWithMaximumSpaceTaken(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      startSlot = {
        HedvigText(stringResource(id = R.string.PROFILE_ABOUT_APP_VERSION))
      },
      endSlot = {
        HedvigText(
          text = buildString {
            append(appVersionName)
            if (!isProduction) {
              append(" (")
              append(appVersionCode)
              append(")")
            }
          },
          color = HedvigTheme.colorScheme.textSecondary,
          textAlign = TextAlign.End,
          modifier = Modifier.fillMaxWidth(),
        )
      },
      spaceBetween = 8.dp,
    )
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = showOpenSourceLicenses)
        .padding(16.dp),
    ) {
      HedvigText(stringResource(R.string.PROFILE_ABOUT_APP_LICENSE_ATTRIBUTIONS))
    }
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.weight(1f))
    HedvigButton(
      text = stringResource(R.string.app_info_submit_bug_button),
      enabled = true,
      buttonStyle = Secondary,
      onClick = { showSubmitBugWarning = true },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun SubmitBugWarningDialog(memberId: String?, appVersionName: String, onDismissRequest: () -> Unit) {
  val localContext = LocalContext.current
  val letterSubject = stringResource(id = R.string.app_info_submit_bug_prefilled_letter_subject)
  val letterBody = String.format(
    stringResource(id = R.string.app_info_submit_bug_prefilled_letter_body),
    memberId,
    appVersionName,
    "Android ${Build.VERSION.SDK_INT}",
  )
  HedvigAlertDialog(
    title = stringResource(id = R.string.app_info_submit_bug_warning),
    text = null,
    onDismissRequest = onDismissRequest,
    onConfirmClick = {
      openEmailClientWithPrefilledData(
        localContext,
        "android@hedvig.com",
        letterSubject,
        letterBody,
      )
    },
    confirmButtonLabel = stringResource(id = R.string.app_info_submit_bug_continue),
    dismissButtonLabel = stringResource(id = R.string.app_info_submit_bug_go_back),
  )
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
      sendLetterIntent,
      letterSubject,
    ),
  )
}

@HedvigPreview
@Composable
private fun PreviewAboutAppScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
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
