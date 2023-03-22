package com.hedvig.android.odyssey.step.dateofoccurrence

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.card.HedvigCardElevation
import com.hedvig.android.core.designsystem.component.datepicker.HedvigDatePicker
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.progress.FullScreenHedvigProgress
import com.hedvig.android.core.ui.snackbar.ErrorSnackbar
import com.hedvig.android.odyssey.data.ClaimFlowStep
import hedvig.resources.R

@Composable
internal fun DateOfOccurrenceDestination(
  viewModel: DateOfOccurrenceViewModel,
  windowSizeClass: WindowSizeClass,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateBack: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val nextStep = uiState.nextStep
  LaunchedEffect(nextStep) {
    if (nextStep != null) {
      navigateToNextStep(nextStep)
    }
  }
  DateOfOccurrenceScreen(
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    submitSelectedDate = viewModel::submitSelectedDate,
    showedError = viewModel::showedError,
    navigateBack = navigateBack,
  )
}

@Composable
private fun DateOfOccurrenceScreen(
  uiState: DateOfOccurrenceUiState,
  windowSizeClass: WindowSizeClass,
  submitSelectedDate: () -> Unit,
  showedError: () -> Unit,
  navigateBack: () -> Unit,
) {
  Box(Modifier.fillMaxSize()) {
    Column {
      val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
      TopAppBarWithBack(
        onClick = navigateBack,
        title = stringResource(R.string.claims_incident_screen_header),
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
        HedvigCard(
          shape = RoundedCornerShape(12.dp),
          elevation = HedvigCardElevation.Elevated(),
          modifier = sideSpacingModifier.padding(end = 16.dp),
        ) {
          Text(
            text = stringResource(R.string.claims_item_screen_date_of_incident_button),
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
          )
        }
        Spacer(Modifier.height(20.dp))
        Spacer(Modifier.weight(1f))
        HedvigCard(modifier = sideSpacingModifier.fillMaxWidth()) {
          HedvigDatePicker(
            datePickerState = uiState.datePickerUiState.datePickerState,
            dateValidator = uiState.datePickerUiState::validateDate,
          )
        }
        Spacer(Modifier.height(16.dp))
        LargeContainedTextButton(
          text = stringResource(R.string.general_continue_button),
          onClick = submitSelectedDate,
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
    FullScreenHedvigProgress(show = uiState.isLoading)
    ErrorSnackbar(
      hasError = uiState.dateSubmissionError,
      showedError = showedError,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .windowInsetsPadding(WindowInsets.safeDrawing),
    )
  }
}
