package com.hedvig.android.ui.claimflow

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.LocalSharedTransitionScope
import com.hedvig.android.compose.ui.globalSharedElement
import com.hedvig.android.compose.ui.rememberGlobalSharedContentState
import com.hedvig.android.design.system.hedvig.ErrorSnackbar
import com.hedvig.android.design.system.hedvig.ErrorSnackbarState
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType.BACK
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.navigation.compose.LocalNavAnimatedVisibilityScope
import hedvig.resources.R

/**
 * An opinionated scaffold to make it easier to make screens for the Claim flow.
 * Sets up a top app bar with a back button, the X button and scrollable content and error/loading visuals.
 */
@Composable
fun ClaimFlowScaffold(
  windowSizeClass: WindowSizeClass,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
  modifier: Modifier = Modifier,
  topAppBarText: String? = null,
  errorSnackbarState: ErrorSnackbarState? = null,
  itemsColumnHorizontalAlignment: Alignment.Horizontal = Alignment.Start,
  content: @Composable (ColumnScope.(sideSpacingModifier: Modifier) -> Unit),
) {
  var showCloseClaimsFlowDialog by rememberSaveable { mutableStateOf(false) }
  if (showCloseClaimsFlowDialog) {
    HedvigAlertDialog(
      title = stringResource(R.string.GENERAL_ARE_YOU_SURE),
      text = stringResource(R.string.claims_alert_body),
      onDismissRequest = { showCloseClaimsFlowDialog = false },
      onConfirmClick = closeClaimFlow,
    )
  }

  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = modifier.fillMaxSize(),
  ) {
    Box {
      Column(Modifier.matchParentSize()) {
        TopAppBar(
          title = topAppBarText ?: "",
          actionType = BACK,
          onActionClick = navigateUp,
          topAppBarActions = {
            IconButton(
              modifier = Modifier.size(24.dp),
              onClick = { showCloseClaimsFlowDialog = true },
              content = {
                Icon(
                  imageVector = HedvigIcons.Close,
                  contentDescription = null,
                )
              },
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .globalSharedElement(
              LocalSharedTransitionScope.current,
              LocalNavAnimatedVisibilityScope.current,
              rememberGlobalSharedContentState("com.hedvig.android.ui.claimflow.ClaimFlowScaffold"),
            ),
        )
        Column(
          horizontalAlignment = itemsColumnHorizontalAlignment,
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        ) {
          val sideSpacingModifier = if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
            Modifier
              .fillMaxWidth(1f)
              .wrapContentWidth(Alignment.CenterHorizontally)
              .fillMaxWidth(0.8f)
          } else {
            Modifier.padding(horizontal = 16.dp)
          }
          content(sideSpacingModifier)
        }
      }
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
}
