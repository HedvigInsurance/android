package com.hedvig.android.feature.profile.contactinfo

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
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.clearFocusOnTap
import com.hedvig.android.feature.profile.contactinfo.ContactInfoEvent.RetryLoadData
import com.hedvig.android.feature.profile.contactinfo.ContactInfoEvent.SubmitData
import com.hedvig.android.feature.profile.contactinfo.ContactInfoUiState.Content
import com.hedvig.android.feature.profile.data.ContactInformation.Email
import com.hedvig.android.feature.profile.data.ContactInformation.PhoneNumber
import hedvig.resources.R

@Composable
internal fun ContactInfoDestination(viewModel: ContactInfoViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ContactInfoScreen(
    uiState = uiState,
    updateEmailAndPhoneNumber = {
      viewModel.emit(SubmitData)
    },
    reload = {
      viewModel.emit(RetryLoadData)
    },
    navigateUp = navigateUp,
  )
}

@Composable
private fun ContactInfoScreen(
  uiState: ContactInfoUiState,
  updateEmailAndPhoneNumber: () -> Unit,
  reload: () -> Unit,
  navigateUp: () -> Unit,
) {
  val focusManager = LocalFocusManager.current
  HedvigScaffold(
    topAppBarText = stringResource(R.string.PROFILE_MY_INFO_ROW_TITLE),
    navigateUp = navigateUp,
    modifier = Modifier
      .fillMaxSize()
      .clearFocusOnTap(),
  ) {
    when (uiState) {
      ContactInfoUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced(Modifier
          .weight(1f)
          .wrapContentHeight())
      }

      ContactInfoUiState.Error -> {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier
          .weight(1f)
          .wrapContentHeight())
      }

      is ContactInfoUiState.Content -> {
        SuccessState(
          uiState = uiState,
          updateEmailAndPhoneNumber = updateEmailAndPhoneNumber,
          focusManager = focusManager,
        )
      }
    }
  }
}

@Composable
private fun ColumnScope.SuccessState(
  uiState: ContactInfoUiState.Content,
  updateEmailAndPhoneNumber: () -> Unit,
  focusManager: FocusManager,
) {
  Spacer(Modifier.weight(1f))
  Spacer(Modifier.height(16.dp))
  HedvigNotificationCard(
    message = stringResource(R.string.PROFILE_MY_INFO_REVIEW_INFO_CARD),
    priority = NotificationDefaults.NotificationPriority.Info,
    modifier = Modifier.padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(4.dp))
  ContactInfoTextField(
    textFieldState = uiState.emailState,
    labelText = stringResource(R.string.PROFILE_MY_INFO_EMAIL_LABEL),
    errorText = stringResource(R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DESCRIPTION_EMAIL).takeIf {
      uiState.emailHasError
    },
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Email,
      imeAction = ImeAction.Done,
    ),
    inputTransformation = uiState.emailInputTransformation,
    keyboardActionHandler = KeyboardActionHandler {
      updateEmailAndPhoneNumber()
      focusManager.clearFocus()
    },
  )
  Spacer(Modifier.height(4.dp))
  ContactInfoTextField(
    textFieldState = uiState.phoneNumberState,
    labelText = stringResource(R.string.PHONE_NUMBER_ROW_TITLE),
    errorText = stringResource(R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DESCRIPTION_PHONE_NUMBER).takeIf {
      uiState.phoneNumberHasError
    },
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Phone,
      imeAction = ImeAction.Next,
    ),
    inputTransformation = uiState.phoneNumberInputTransformation,
    keyboardActionHandler = null,
  )
  Spacer(Modifier.height(16.dp))
  HedvigButton(
    text = stringResource(R.string.general_save_button),
    enabled = uiState.canSubmit || uiState.submittingUpdatedInfo,
    onClick = {
      focusManager.clearFocus()
      updateEmailAndPhoneNumber()
    },
    isLoading = uiState.submittingUpdatedInfo,
    modifier = Modifier
      .padding(horizontal = 16.dp)
      .fillMaxSize(),
  )
  Spacer(Modifier.height(16.dp))
}

@Composable
private fun ContactInfoTextField(
  textFieldState: TextFieldState,
  labelText: String,
  errorText: String?,
  keyboardOptions: KeyboardOptions,
  inputTransformation: InputTransformation,
  keyboardActionHandler: KeyboardActionHandler?,
) {
  HedvigTextField(
    state = textFieldState,
    labelText = labelText,
    errorState = if (errorText == null) {
      HedvigTextFieldDefaults.ErrorState.NoError
    } else {
      HedvigTextFieldDefaults.ErrorState.Error.WithMessage(errorText)
    },
    keyboardOptions = keyboardOptions,
    inputTransformation = inputTransformation,
    keyboardActions = keyboardActionHandler,
    textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
}

@HedvigPreview
@Composable
private fun PreviewContactInfoScreen(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) withErrors: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ContactInfoScreen(
        uiState = Content(
          rememberTextFieldState(),
          rememberTextFieldState(),
          PhoneNumber("072102103".takeIf { !withErrors } ?: ""),
          Email("email@email.com".takeIf { !withErrors } ?: ""),
          false,
        ),
        updateEmailAndPhoneNumber = {},
        reload = {},
        navigateUp = {},
      )
    }
  }
}
