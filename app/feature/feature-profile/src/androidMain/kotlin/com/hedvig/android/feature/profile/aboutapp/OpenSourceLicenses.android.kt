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
import com.hedvig.android.composewebview.WebView
import com.hedvig.android.composewebview.rememberWebViewState

private const val licensesUrl = "file:///android_asset/open_source_licenses.html"

@Composable
internal actual fun OpenSourceLicenses(modifier: Modifier) {
  Column(
    modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
  ) {
    val webViewState = rememberWebViewState(licensesUrl)
    WebView(
      state = webViewState,
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(
          WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
        ),
    )
  }
}
