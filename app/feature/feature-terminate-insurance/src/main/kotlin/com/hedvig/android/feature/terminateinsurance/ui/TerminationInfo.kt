package com.hedvig.android.feature.terminateinsurance.ui

import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.designsystem.preview.HedvigMultiScreenPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.WarningFilled
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.preview.calculateForPreview
import hedvig.resources.R

/**
 * Skeleton for any kind of summary screen about success and failure
 */
@Composable
internal fun TerminationInfoScreen(
  windowSizeClass: WindowSizeClass,
  title: String,
  headerText: String,
  bodyText: String,
  navigateUp: () -> Unit,
  bottomContent: @Composable () -> Unit,
) {
  Column {
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    TopAppBarWithBack(
      onClick = navigateUp,
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
        imageVector = Icons.Hedvig.WarningFilled,
        contentDescription = "Icon",
        tint = MaterialTheme.colorScheme.warningElement,
        modifier = sideSpacingModifier
          .align(Alignment.CenterHorizontally),
      )
      Spacer(Modifier.height(16.dp))
      Text(
        text = headerText,
        textAlign = TextAlign.Center,
        style = LocalTextStyle.current.copy(
          lineBreak = LineBreak.Heading,
        ),
        modifier = sideSpacingModifier.align(Alignment.CenterHorizontally),
      )
      Spacer(Modifier.height(16.dp))
      CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
        Text(
          text = bodyText,
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center,
          modifier = sideSpacingModifier.align(Alignment.CenterHorizontally),
        )
      }
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.weight(1f))
      Box(sideSpacingModifier) {
        bottomContent()
      }
      Spacer(Modifier.height(16.dp))
      Spacer(
        Modifier
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
          .align(Alignment.CenterHorizontally),
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
        navigateUp = {},
      ) {
        HedvigContainedButton(
          text = stringResource(R.string.general_done_button),
          onClick = { },
        )
      }
    }
  }
}
