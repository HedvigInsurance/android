package com.hedvig.android.feature.odyssey.step.location

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.ui.snackbar.ErrorSnackbar
import com.hedvig.android.feature.odyssey.data.ClaimFlowStep
import hedvig.resources.R

@Composable
internal fun LocationDestination(
  viewModel: LocationViewModel,
  windowSizeClass: WindowSizeClass,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateBack: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val claimFlowStep = uiState.nextStep
  LaunchedEffect(claimFlowStep) {
    if (claimFlowStep != null) {
      navigateToNextStep(claimFlowStep)
    }
  }
  LocationScreen(
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    selectLocation = viewModel::selectLocationOption,
    submitLocation = viewModel::submitLocation,
    showedError = viewModel::showedError,
    navigateBack = navigateBack,
  )
}

@Composable
private fun LocationScreen(
  uiState: LocationUiState,
  windowSizeClass: WindowSizeClass,
  selectLocation: (String) -> Unit,
  submitLocation: () -> Unit,
  showedError: () -> Unit,
  navigateBack: () -> Unit,
) {
  Box(Modifier.fillMaxSize()) {
    Column {
      val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
      TopAppBarWithBack(
        onClick = navigateBack,
        title = stringResource(R.string.claims_location_screen_title),
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
        Spacer(Modifier.height(20.dp))
        HedvigCard(sideSpacingModifier) {
          Column {
            uiState.locationOptions.forEachIndexed { index, locationOption ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    selectLocation(locationOption.value)
                  }
                  .padding(16.dp),
              ) {
                Text(locationOption.value, Modifier.align(Alignment.CenterVertically))
                Spacer(Modifier.weight(1f))
                val iconAlpha by animateFloatAsState(
                  targetValue = if (locationOption.value == uiState.selectedLocation) 1f else 0f,
                  label = "iconAlpha",
                )
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  modifier = Modifier.graphicsLayer { alpha = iconAlpha },
                )
              }
              if (index != uiState.locationOptions.lastIndex) {
                Divider(Modifier.padding(horizontal = 8.dp))
              }
            }
          }
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        LargeContainedTextButton(
          text = stringResource(R.string.general_continue_button),
          onClick = submitLocation,
          enabled = uiState.canSubmit,
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
    HedvigFullScreenCenterAlignedProgress(show = uiState.isLoading)
    ErrorSnackbar(
      hasError = uiState.error,
      showedError = showedError,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .windowInsetsPadding(WindowInsets.safeDrawing),
    )
  }
}
