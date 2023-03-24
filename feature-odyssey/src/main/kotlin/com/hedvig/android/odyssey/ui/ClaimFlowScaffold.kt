package com.hedvig.android.odyssey.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.progress.FullScreenHedvigProgress
import com.hedvig.android.core.ui.snackbar.ErrorSnackbar
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState

/**
 * An opinionated scaffold to make it easier to make screens for the Claim flow.
 * Sets up a top app bar with a back button, scrollable content and error/loading visuals.
 */
@Composable
internal fun ClaimFlowScaffold(
  windowSizeClass: WindowSizeClass,
  navigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  topAppBarText: String? = null,
  isLoading: Boolean = false,
  errorSnackbarState: ErrorSnackbarState? = null,
  content: @Composable() (ColumnScope.(sideSpacingModifier: Modifier) -> Unit),
) {
  Box(modifier.fillMaxSize()) {
    Column {
      val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
      TopAppBarWithBack(
        onClick = navigateBack,
        title = topAppBarText ?: "",
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
        content(sideSpacingModifier)
      }
    }
    FullScreenHedvigProgress(show = isLoading)
    if (errorSnackbarState != null) {
      ErrorSnackbar(
        errorSnackbarState = errorSnackbarState,
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .windowInsetsPadding(WindowInsets.safeDrawing),
      )
    }
  }
}
