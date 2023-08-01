package com.hedvig.android.feature.forever.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.apollo.format
import com.hedvig.android.apollo.toWebLocaleTag
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.feature.forever.ForeverUiState
import com.hedvig.android.feature.forever.showShareSheet
import com.hedvig.android.language.LanguageService
import hedvig.resources.R

@Composable
internal fun ForeverScreen(
  uiState: ForeverUiState,
  reload: () -> Unit,
  onSubmitCode: (String) -> Unit,
  onCodeChanged: (String) -> Unit,
  languageService: LanguageService,
) {
  Box(
    modifier = Modifier.fillMaxSize(),
    propagateMinConstraints = true,
  ) {
    val context = LocalContext.current
    val resources = context.resources
    if (uiState.errorMessage != null) {
      Box {
        Column(Modifier.matchParentSize()) {
          Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
          Spacer(Modifier.height(64.dp))
          HedvigErrorSection(retry = reload)
        }
      }
    } else {
      val shareSheetTitle = stringResource(R.string.REFERRALS_SHARE_SHEET_TITLE)
      ForeverContent(
        uiState = uiState,
        reload = reload,
        onShareCodeClick = { code, incentive ->
          context.showShareSheet(shareSheetTitle) { intent ->
            intent.putExtra(
              Intent.EXTRA_TEXT,
              resources.getString(
                R.string.REFERRAL_SMS_MESSAGE,
                incentive.format(languageService.getLocale()),
                buildString {
                  append("https://www.dev.hedvigit.com") // TODO Get from resources
                  append("/")
                  append(languageService.getGraphQLLocale().toWebLocaleTag())
                  append("/forever/")
                  append(Uri.encode(code))
                },
              ),
            )
            intent.type = "text/plain"
          }
        },
        onCodeChanged = onCodeChanged,
        onSubmitCode = onSubmitCode,
      )
    }
  }
}
