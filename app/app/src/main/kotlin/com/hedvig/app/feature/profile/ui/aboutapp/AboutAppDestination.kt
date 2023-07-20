package com.hedvig.app.feature.profile.ui.aboutapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ChevronRight
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.app.BuildConfig
import com.hedvig.app.feature.embark.ui.MemberIdViewModel
import hedvig.resources.R

@Composable
internal fun AboutAppDestination(
  viewModel: MemberIdViewModel,
  onBackPressed: () -> Unit,
  showOpenSourceLicenses: () -> Unit,
  isProduction: Boolean,
) {
  val memberIdState by viewModel.state.collectAsStateWithLifecycle()
  val memberId: String? = when (val latestMemberIdState = memberIdState) {
    is MemberIdViewModel.State.Success -> latestMemberIdState.id
    else -> null
  }

  AboutAppScreen(
    memberId = memberId,
    onBackPressed = onBackPressed,
    showOpenSourceLicenses = showOpenSourceLicenses,
    isProduction = isProduction,
  )
}

@Composable
private fun AboutAppScreen(
  memberId: String?,
  onBackPressed: () -> Unit,
  showOpenSourceLicenses: () -> Unit,
  isProduction: Boolean,
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
        title = stringResource(R.string.PROFILE_ABOUT_APP_TITLE),
        contentPadding = WindowInsets.safeDrawing
          .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
          .asPaddingValues(),
      )
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
                append(BuildConfig.VERSION_NAME)
                if (!isProduction) {
                  append(" (")
                  append(BuildConfig.VERSION_CODE)
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
          CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            Icon(
              imageVector = Hedvig.ChevronRight,
              contentDescription = "",
            )
          }
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewAboutAppScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AboutAppScreen("123", {}, {}, false)
    }
  }
}
