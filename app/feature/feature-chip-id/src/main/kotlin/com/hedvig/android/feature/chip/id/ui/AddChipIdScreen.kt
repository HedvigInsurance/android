package com.hedvig.android.feature.chip.id.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.byValue
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.clearFocusOnTap
import com.hedvig.android.feature.chip.id.ui.AddChipIdEvent.RetryLoadData
import com.hedvig.android.feature.chip.id.ui.AddChipIdEvent.SubmitData
import com.hedvig.android.feature.chip.id.ui.AddChipIdUiState.Content
import hedvig.resources.CONTACT_INFO_CHANGES_SAVED
import hedvig.resources.Res
import hedvig.resources.general_save_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AddChipIdDestination(
  viewModel: AddChipIdViewModel,
  globalSnackBarState: GlobalSnackBarState,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
) {
  val uiState: AddChipIdUiState by viewModel.uiState.collectAsStateWithLifecycle()
  AddChipIdScreen(
    uiState = uiState,
    globalSnackBarState = globalSnackBarState,
    submitChipId = {
      viewModel.emit(SubmitData)
    },
    reload = {
      viewModel.emit(RetryLoadData)
    },
    navigateUp = navigateUp,
    showedSnackBar = {
      viewModel.emit(AddChipIdEvent.ShowedMessage)
      popBackStack()
    },
  )
}

@Composable
private fun AddChipIdScreen(
  uiState: AddChipIdUiState,
  globalSnackBarState: GlobalSnackBarState,
  submitChipId: () -> Unit,
  reload: () -> Unit,
  navigateUp: () -> Unit,
  showedSnackBar: () -> Unit,
) {
  val focusManager = LocalFocusManager.current
  HedvigScaffold(
    topAppBarText = "Update pet Chip-ID", //todo
    navigateUp = navigateUp,
    modifier = Modifier
      .fillMaxSize()
      .clearFocusOnTap(),
  ) {
    when (uiState) {
      AddChipIdUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced(
          Modifier
            .weight(1f)
            .wrapContentHeight(),
        )
      }

      AddChipIdUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = reload,
          modifier = Modifier
            .weight(1f)
            .wrapContentHeight(),
        )
      }

      is Content -> {
        AddChipIdContent(
          uiState = uiState,
          globalSnackBarState = globalSnackBarState,
          submitChipId = submitChipId,
          focusManager = focusManager,
          showedSnackBar = showedSnackBar,
        )
      }
    }
  }
}

@Composable
private fun ColumnScope.AddChipIdContent(
  uiState: Content,
  globalSnackBarState: GlobalSnackBarState,
  submitChipId: () -> Unit,
  focusManager: FocusManager,
  showedSnackBar: () -> Unit,
) {
  val successMessage = stringResource(Res.string.CONTACT_INFO_CHANGES_SAVED)
  LaunchedEffect(uiState.showSuccessSnackBar) {
    if (!uiState.showSuccessSnackBar) return@LaunchedEffect
    globalSnackBarState.show(successMessage, NotificationPriority.Campaign)
    showedSnackBar()
  }

  Spacer(Modifier.weight(1f))
  Spacer(Modifier.height(16.dp))

  ChipIdTextField(
    textFieldState = uiState.chipIdState,
    labelText = "Chip ID", //todo
    keyboardActionHandler = KeyboardActionHandler {
      submitChipId()
      focusManager.clearFocus()
    },
  )

  AnimatedContent(
    targetState = uiState.errorType,
    transitionSpec = { fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically() },
    modifier = Modifier.padding(top = 4.dp),
  ) { errorType ->
    if (errorType != null) {
      val errorMessage = when (errorType) {
        ChipIdErrorType.WrongInput -> "Must be 15 digits" //todo
        ChipIdErrorType.GeneralError -> "Something went wrong" //todo
      }
      HedvigNotificationCard(
        message = errorMessage,
        priority = NotificationPriority.Error,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
      )
    }
  }

  Spacer(Modifier.height(16.dp))
  HedvigButton(
    text = stringResource(Res.string.general_save_button),
    enabled = !uiState.submittingData,
    onClick = {
      focusManager.clearFocus()
      submitChipId()
    },
    isLoading = uiState.submittingData,
    modifier = Modifier
      .padding(horizontal = 16.dp)
      .fillMaxSize(),
  )
  Spacer(Modifier.height(16.dp))
}

@Composable
private fun ChipIdTextField(
  textFieldState: TextFieldState,
  labelText: String,
  keyboardActionHandler: KeyboardActionHandler?,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val digitsOnlyTransformation = InputTransformation.byValue { _, proposed ->
    proposed.filter { it.isDigit() }
  }
  HedvigTextField(
    state = textFieldState,
    labelText = labelText,
    errorState = HedvigTextFieldDefaults.ErrorState.NoError,
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Number,
      imeAction = ImeAction.Done,
    ),
    inputTransformation = digitsOnlyTransformation,
    keyboardActions = keyboardActionHandler,
    textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
    interactionSource = interactionSource,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
}
