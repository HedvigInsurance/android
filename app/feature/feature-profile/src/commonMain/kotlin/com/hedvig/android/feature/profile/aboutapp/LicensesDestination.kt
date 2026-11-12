package com.hedvig.android.feature.profile.aboutapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import hedvig.resources.PROFILE_ABOUT_APP_LICENSE_ATTRIBUTIONS
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LicensesDestination(onBackPressed: () -> Unit) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.fillMaxSize()
      .windowInsetsPadding(
        WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
      ),
  ) {
    Column {
      TopAppBarWithBack(
        onClick = onBackPressed,
        title = stringResource(Res.string.PROFILE_ABOUT_APP_LICENSE_ATTRIBUTIONS),
      )
      OpenSourceLicenses()
    }
  }
}

/**
 * The open source licenses of the app's dependencies, filling the space below the top app bar.
 */
@Composable
internal expect fun OpenSourceLicenses(modifier: Modifier = Modifier)
