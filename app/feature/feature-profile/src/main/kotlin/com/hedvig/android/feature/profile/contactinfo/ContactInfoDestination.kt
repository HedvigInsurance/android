package com.hedvig.android.feature.profile.contactinfo

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.clearFocusOnTap
import com.hedvig.android.feature.profile.contactinfo.ContactInfoEvent.RetryLoadData
import com.hedvig.android.feature.profile.contactinfo.ContactInfoEvent.SubmitData
import com.hedvig.android.feature.profile.contactinfo.ContactInfoUiState.Content
import hedvig.resources.CONTACT_INFO_CHANGES_SAVED
import hedvig.resources.PHONE_NUMBER_ROW_TITLE
import hedvig.resources.PROFILE_MY_INFO_EMAIL_LABEL
import hedvig.resources.PROFILE_MY_INFO_REVIEW_INFO_CARD
import hedvig.resources.PROFILE_MY_INFO_ROW_TITLE
import hedvig.resources.PROFILE_MY_INFO_VALIDATION_DIALOG_DESCRIPTION_EMAIL
import hedvig.resources.PROFILE_MY_INFO_VALIDATION_DIALOG_DESCRIPTION_PHONE_NUMBER
import hedvig.resources.Res
import hedvig.resources.general_save_button
import hedvig.resources.something_went_wrong
import hedvig.resources.travel_certificate_travel_certificate_ready
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactInfoDestination(
  viewModel: ContactInfoViewModel,
  globalSnackBarState: GlobalSnackBarState,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ContactInfoScreen(
    uiState = uiState,
    globalSnackBarState = globalSnackBarState,
    updateEmailAndPhoneNumber = {
      viewModel.emit(SubmitData)
    },
    reload = {
      viewModel.emit(RetryLoadData)
    },
    navigateUp = navigateUp,
    showedSnackBar = {
      viewModel.emit(ContactInfoEvent.ShowedMessage)
      popBackStack()
    },
  )
}

@Composable
private fun ContactInfoScreen(
  uiState: ContactInfoUiState,
  globalSnackBarState: GlobalSnackBarState,
  updateEmailAndPhoneNumber: () -> Unit,
  reload: () -> Unit,
  navigateUp: () -> Unit,
  showedSnackBar: () -> Unit,
) {
  val focusManager = LocalFocusManager.current
  HedvigScaffold(
    topAppBarText = stringResource(Res.string.PROFILE_MY_INFO_ROW_TITLE),
    navigateUp = navigateUp,
    modifier = Modifier
      .fillMaxSize()
      .clearFocusOnTap(),
  ) {
    when (uiState) {
      ContactInfoUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced(
          Modifier
            .weight(1f)
            .wrapContentHeight(),
        )
      }

      ContactInfoUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = reload,
          modifier = Modifier
            .weight(1f)
            .wrapContentHeight(),
        )
      }

      is Content -> {
        SuccessState(
          uiState = uiState,
          globalSnackBarState = globalSnackBarState,
          updateEmailAndPhoneNumber = updateEmailAndPhoneNumber,
          focusManager = focusManager,
          showedSnackBar = showedSnackBar,
        )
      }
    }
  }
}

@Composable
private fun ColumnScope.SuccessState(
  uiState: Content,
  globalSnackBarState: GlobalSnackBarState,
  updateEmailAndPhoneNumber: () -> Unit,
  showedSnackBar: () -> Unit,
  focusManager: FocusManager,
) {
  val errorCardText: String? = when (val err = uiState.errorSnackBarText) {
    ErrorSnackBarText.General -> stringResource(Res.string.something_went_wrong)
    is ErrorSnackBarText.WithMessage -> err.message
    null -> null
  }
  val travelCertificateReadyText = stringResource(Res.string.CONTACT_INFO_CHANGES_SAVED)
  LaunchedEffect(uiState.showSuccessSnackBar) {
    if (!uiState.showSuccessSnackBar) return@LaunchedEffect
    globalSnackBarState.show(travelCertificateReadyText)
    showedSnackBar()
  }
  Spacer(Modifier.weight(1f))
  Spacer(Modifier.height(16.dp))
  HedvigNotificationCard(
    message = stringResource(Res.string.PROFILE_MY_INFO_REVIEW_INFO_CARD),
    priority = NotificationPriority.Info,
    modifier = Modifier.padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(4.dp))
  Column {
    ContactInfoTextField(
      textFieldState = uiState.emailState,
      labelText = stringResource(Res.string.PROFILE_MY_INFO_EMAIL_LABEL),
      errorText = stringResource(Res.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DESCRIPTION_EMAIL).takeIf {
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
      labelText = stringResource(Res.string.PHONE_NUMBER_ROW_TITLE),
      errorText = stringResource(Res.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DESCRIPTION_PHONE_NUMBER).takeIf {
        uiState.phoneNumberHasError
      },
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Phone,
        imeAction = ImeAction.Next,
      ),
      inputTransformation = uiState.phoneNumberInputTransformation,
      keyboardActionHandler = null,
    )
  }
  AnimatedContent(
    targetState = errorCardText,
    transitionSpec = { fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically() },
    modifier = Modifier.padding(top = 4.dp),
  ) { text ->
    if (text != null) {
      HedvigNotificationCard(
        message = text,
        priority = NotificationPriority.Error,
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
      )
    }
  }
  Spacer(Modifier.height(16.dp))
  HedvigButton(
    text = stringResource(Res.string.general_save_button),
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
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()
  HedvigTextField(
    state = textFieldState,
    labelText = labelText,
    errorState = if (errorText == null || isFocused) {
      HedvigTextFieldDefaults.ErrorState.NoError
    } else {
      HedvigTextFieldDefaults.ErrorState.Error.WithMessage(errorText)
    },
    keyboardOptions = keyboardOptions,
    inputTransformation = inputTransformation,
    keyboardActions = keyboardActionHandler,
    textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
    interactionSource = interactionSource,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
}

@HedvigPreview
@Composable
private fun PreviewContactInfoScreen(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) filled: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ContactInfoScreen(
        uiState = Content(
          rememberTextFieldState(if (filled) "072102103" else ""),
          rememberTextFieldState(if (filled) "emaildqd@gmail.com" else ""),
          null,
          null,
          false,
        ),
        globalSnackBarState = GlobalSnackBarState(),
        updateEmailAndPhoneNumber = {},
        reload = {},
        navigateUp = {},
        {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewContactInfoScreenFailure() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ContactInfoScreen(
        uiState = ContactInfoUiState.Error,
        globalSnackBarState = GlobalSnackBarState(),
        updateEmailAndPhoneNumber = {},
        reload = {},
        navigateUp = {},
        {},
      )
    }
  }
}
