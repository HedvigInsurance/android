package com.hedvig.app.feature.profile.ui.aboutapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import hedvig.resources.R

private const val licensesUrl = "file:///android_asset/open_source_licenses.html"

@Composable
fun LicensesDestination(onBackPressed: () -> Unit) {
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
        title = stringResource(R.string.PROFILE_ABOUT_APP_LICENSE_ATTRIBUTIONS),
        contentPadding = WindowInsets.safeDrawing
          .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
          .asPaddingValues(),
      )
      val state = rememberWebViewState(licensesUrl)
      WebView(state = state)
    }
  }
}
