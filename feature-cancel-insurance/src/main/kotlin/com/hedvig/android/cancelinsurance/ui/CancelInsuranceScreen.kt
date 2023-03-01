package com.hedvig.android.cancelinsurance.ui

import android.content.res.Configuration
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
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.card.HedvigCardElevation
import com.hedvig.android.core.designsystem.component.datepicker.HedvigDatePicker
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.snackbar.ErrorSnackbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelInsuranceScreen(
  windowSizeClass: WindowSizeClass,
  datePickerState: DatePickerState,
  dateValidator: (Long) -> Boolean,
  canSubmit: Boolean,
  submit: () -> Unit,
  hasError: Boolean,
  showedError: () -> Unit,
  navigateBack: () -> Unit,
) {
  Box(Modifier.fillMaxSize()) {
    Column {
      val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
      TopAppBarWithBack(
        onClick = navigateBack,
        title = "Set termination date",
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
        ChatCard(sideSpacingModifier)
        Spacer(Modifier.height(20.dp))
        Spacer(Modifier.weight(1f))
        DatePickerCard(
          datePickerState = datePickerState,
          dateValidator = dateValidator,
          modifier = sideSpacingModifier,
        )
        Spacer(Modifier.height(16.dp))
        LargeContainedTextButton(
          text = stringResource(hedvig.resources.R.string.general_continue_button),
          onClick = submit,
          enabled = canSubmit,
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
    ErrorSnackbar(
      hasError = hasError,
      showedError = showedError,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .windowInsetsPadding(WindowInsets.safeDrawing),
    )
  }
}

@Composable
private fun ChatCard(modifier: Modifier = Modifier) {
  HedvigCard(
    shape = RoundedCornerShape(12.dp),
    elevation = HedvigCardElevation.Elevated(),
    modifier = modifier.padding(end = 16.dp),
  ) {
    Text(
      text = "Please set termination date for your insurance.",
      modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
    ) // TODO Add parameter for insurance name
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerCard(
  datePickerState: DatePickerState,
  dateValidator: (Long) -> Boolean,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    modifier = modifier.fillMaxWidth(),
  ) {
    HedvigDatePicker(
      datePickerState = datePickerState,
      dateValidator = dateValidator,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CancelInsuranceScreenPreview() {
  HedvigTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
    ) {
      CancelInsuranceScreen(
        WindowSizeClass.calculateFromSize(DpSize(500.dp, 300.dp)),
        rememberDatePickerState(),
        { true },
        true,
        {},
        false,
        {},
        {},
      )
    }
  }
}
