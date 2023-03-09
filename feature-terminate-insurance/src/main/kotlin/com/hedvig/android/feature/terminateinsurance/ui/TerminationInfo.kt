package com.hedvig.android.feature.terminateinsurance.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.R
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.preview.HedvigMultiScreenPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.preview.calculateForPreview

@Composable
fun TerminationInfoScreen(
  windowSizeClass: WindowSizeClass,
  title: String,
  headerText: String,
  bodyText: String,
  @DrawableRes icon: Int,
  buttonText: String = stringResource(hedvig.resources.R.string.general_done_button),
  onPrimaryButton: () -> Unit,
  navigateBack: () -> Unit,
) {
  Column {
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    TopAppBarWithBack(
      onClick = navigateBack,
      title = title,
      scrollBehavior = topAppBarScrollBehavior,
    )
    Column(
      Modifier
        .fillMaxSize()
        .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
        .verticalScroll(rememberScrollState())
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
      val sideSpacingModifier = if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
        Modifier
          .fillMaxWidth(0.8f)
          .wrapContentWidth(Alignment.Start)
          .align(Alignment.CenterHorizontally)
      } else {
        Modifier.padding(horizontal = 16.dp)
      }
      Spacer(Modifier.height(40.dp))
      Icon(
        painter = painterResource(icon),
        contentDescription = "Icon",
        modifier = sideSpacingModifier.size(32.dp),
      )
      Spacer(Modifier.height(16.dp))
      Text(
        text = headerText,
        style = MaterialTheme.typography.headlineSmall,
        modifier = sideSpacingModifier,
      )
      Spacer(Modifier.height(16.dp))
      Text(
        text = bodyText,
        style = MaterialTheme.typography.bodyLarge,
        modifier = sideSpacingModifier,
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.weight(1f))
      LargeContainedTextButton(
        text = buttonText,
        onClick = onPrimaryButton,
        modifier = sideSpacingModifier,
      )
      Spacer(Modifier.height(16.dp))
      Spacer(
        Modifier.windowInsetsPadding(
          WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
        ),
      )
    }
  }
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewTerminationInfoScreen() {
  HedvigTheme {
    Surface {
      TerminationInfoScreen(
        WindowSizeClass.calculateForPreview(),
        title = "",
        headerText = "Cancellation successful",
        bodyText = """
          Your insurance with Hedvig will be cancelled on 31-10-2022. We'll send you a confirmation email with all the details.

          Thanks for being part of Hedvig and trusting us to protect you and your loved ones when needed. The doors are always open if you decide to come back in the near future.
        """.trimIndent(),
        onPrimaryButton = {},
        icon = R.drawable.ic_checkmark_in_circle,
      ) {}
    }
  }
}
