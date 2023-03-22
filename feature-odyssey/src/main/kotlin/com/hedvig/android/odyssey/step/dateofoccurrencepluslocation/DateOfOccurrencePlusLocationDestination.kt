package com.hedvig.android.odyssey.step.dateofoccurrencepluslocation

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.FormRowCard
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.progress.FullScreenHedvigProgress
import com.hedvig.android.core.ui.snackbar.ErrorSnackbar
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.navigation.LocationOption
import com.hedvig.android.odyssey.ui.DatePickerRowCard
import com.hedvig.android.odyssey.ui.DatePickerUiState
import hedvig.resources.R

@Composable
internal fun DateOfOccurrencePlusLocationDestination(
  viewModel: DateOfOccurrencePlusLocationViewModel,
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
  DateOfOccurrencePlusLocationScreen(
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    selectLocationOption = viewModel::selectLocationOption,
    submitDateOfOccurrenceAndLocation = viewModel::submitDateOfOccurrenceAndLocation,
    showedError = viewModel::showedError,
    navigateBack = navigateBack,
  )
}

@Composable
private fun DateOfOccurrencePlusLocationScreen(
  uiState: DateOfOccurrencePlusLocationUiState,
  windowSizeClass: WindowSizeClass,
  selectLocationOption: (LocationOption) -> Unit,
  submitDateOfOccurrenceAndLocation: () -> Unit,
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
        Spacer(Modifier.height(32.dp))
        DateOfIncident(uiState.datePickerUiState, !uiState.isLoading, sideSpacingModifier)
        Spacer(Modifier.height(20.dp))
        Location(uiState, selectLocationOption, sideSpacingModifier)
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(20.dp))
        LargeContainedTextButton(
          text = stringResource(R.string.general_continue_button),
          onClick = submitDateOfOccurrenceAndLocation,
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
      hasError = uiState.error,
      showedError = showedError,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .windowInsetsPadding(WindowInsets.safeDrawing),
    )
  }
}

@Composable
private fun DateOfIncident(
  uiState: DatePickerUiState,
  canInteract: Boolean,
  modifier: Modifier = Modifier,
) {
  DatePickerRowCard(
    uiState = uiState,
    canInteract = canInteract,
    startText = stringResource(R.string.claims_item_screen_date_of_incident_button),
    modifier = modifier,
  )
}

@Composable
private fun Location(
  uiState: DateOfOccurrencePlusLocationUiState,
  selectLocationOption: (LocationOption) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showLocationPickerDialog by rememberSaveable { mutableStateOf(false) }
  if (showLocationPickerDialog) {
    AlertDialog(
      onDismissRequest = { showLocationPickerDialog = false },
      title = {
        Text(stringResource(R.string.claims_location_screen_title))
      },
      confirmButton = {
        TextButton(
          onClick = { showLocationPickerDialog = false },
          shape = MaterialTheme.shapes.medium,
        ) {
          Text(stringResource(R.string.ALERT_OK))
        }
      },
      text = {
        HedvigCard {
          Column {
            uiState.locationOptions.forEachIndexed { index, locationOption ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { selectLocationOption(locationOption) }
                  .padding(16.dp),
              ) {
                Text(locationOption.displayName)
                Spacer(Modifier.weight(1f))
                val iconAlpha by animateFloatAsState(
                  targetValue = if (locationOption.value == uiState.selectedLocation?.value) 1f else 0f,
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
      },
    )
  }

  FormRowCard(
    enabled = !uiState.isLoading,
    onClick = { showLocationPickerDialog = true },
    modifier = modifier,
  ) {
    Text(stringResource(R.string.claims_location_screen_title))
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.width(8.dp))
    Text(uiState.selectedLocation?.displayName ?: stringResource(R.string.payments_history_select_button))
    Spacer(Modifier.width(12.dp))
    Icon(Icons.Default.ArrowForward, null)
  }
}

@HedvigPreview
@Composable
private fun PreviewDateOfOccurrencePlusLocationScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      DateOfOccurrencePlusLocationScreen(
        uiState = DateOfOccurrencePlusLocationUiState(
          datePickerUiState = remember { DatePickerUiState(null) },
          locationOptions = emptyList(),
          selectedLocation = null,
        ),
        windowSizeClass = WindowSizeClass.calculateForPreview(),
        selectLocationOption = {},
        submitDateOfOccurrenceAndLocation = {},
        showedError = {},
        navigateBack = {},
      )
    }
  }
}
