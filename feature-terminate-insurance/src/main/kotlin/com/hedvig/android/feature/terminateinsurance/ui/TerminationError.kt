package com.hedvig.android.feature.terminateinsurance.ui

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
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack

@Composable
fun TerminationErrorScreen(
  windowSizeClass: WindowSizeClass,
  errorMessage: String,
  navigateBack: () -> Unit,
) {
  Column {
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    TopAppBarWithBack(
      onClick = navigateBack,
      title = "",
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
        painter = painterResource(R.drawable.ic_warning_triangle),
        contentDescription = "Checkmark in circle",
        modifier = sideSpacingModifier.size(32.dp),
      )
      Spacer(Modifier.height(16.dp))
      Text(
        text = "Cancellation unsuccessful",
        style = MaterialTheme.typography.headlineSmall,
        modifier = sideSpacingModifier,
      )
      Spacer(Modifier.height(16.dp))
      Text(
        text = errorMessage,
        style = MaterialTheme.typography.bodyLarge,
        modifier = sideSpacingModifier,

        )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.weight(1f))
      LargeContainedTextButton(
        text = stringResource(hedvig.resources.R.string.general_done_button),
        onClick = navigateBack,
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
