package com.hedvig.android.feature.profile.aboutapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hedvig.android.composewebview.WebView
import com.hedvig.android.composewebview.rememberWebViewState
import hedvig.resources.R

private const val licensesUrl = "file:///android_asset/open_source_licenses.html"

@Composable
internal fun LicensesDestination(onBackPressed: () -> Unit) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column {
      TopAppBarWithBack(
        onClick = onBackPressed,
        title = stringResource(R.string.PROFILE_ABOUT_APP_LICENSE_ATTRIBUTIONS),
      )
      Column(
        Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
      ) {
        val webViewState = rememberWebViewState(licensesUrl)
        WebView(
          state = webViewState,
          modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
              WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
            ),
        )
      }
    }
  }
}
