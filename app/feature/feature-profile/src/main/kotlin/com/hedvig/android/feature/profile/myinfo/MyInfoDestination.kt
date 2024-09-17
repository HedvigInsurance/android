package com.hedvig.android.feature.profile.myinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.FadeAnimatedContent
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.clearFocusOnTap
import com.hedvig.android.feature.profile.myinfo.MyInfoUiState.Success
import hedvig.resources.R

@Composable
internal fun MyInfoDestination(viewModel: MyInfoViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  MyInfoScreen(
    uiState = uiState,
    emailChanged = {
      viewModel.emit(MyInfoEvent.EmailChanged(it))
    },
    phoneNumberChanged = {
      viewModel.emit(MyInfoEvent.PhoneNumberChanged(it))
    },
    updateEmailAndPhoneNumber = {
      viewModel.emit(MyInfoEvent.UpdateEmailAndPhoneNumber)
    },
    reload = {
      viewModel.emit(MyInfoEvent.Reload)
    },
    navigateUp = navigateUp,
  )
}

@Composable
private fun MyInfoScreen(
  uiState: MyInfoUiState,
  updateEmailAndPhoneNumber: () -> Unit,
  navigateUp: () -> Unit,
  emailChanged: (String) -> Unit,
  phoneNumberChanged: (String) -> Unit,
  reload: () -> Unit,
) {
  val focusManager = LocalFocusManager.current
  Box(
    modifier = Modifier.fillMaxSize(),
  ) {
    HedvigScaffold(
      topAppBarText = stringResource(R.string.PROFILE_MY_INFO_ROW_TITLE),
      navigateUp = navigateUp,
      modifier = Modifier.clearFocusOnTap(),
    ) {
      FadeAnimatedContent(
        targetState = uiState,
        contentKey = { it::class },
        modifier = Modifier.weight(1f),
      ) { animatedUiState ->
        Column(
          Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState()),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          when (animatedUiState) {
            MyInfoUiState.Loading -> {
              Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                HedvigFullScreenCenterAlignedProgressDebounced()
              }
            }

            MyInfoUiState.Error -> {
              Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                HedvigErrorSection(onButtonClick = reload)
              }
            }

            is Success -> {
              SuccessState(animatedUiState, phoneNumberChanged, emailChanged, updateEmailAndPhoneNumber, focusManager)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ColumnScope.SuccessState(
  uiState: Success,
  phoneNumberChanged: (String) -> Unit,
  emailChanged: (String) -> Unit,
  updateEmailAndPhoneNumber: () -> Unit,
  focusManager: FocusManager,
) {
  var emailInput by rememberSaveable { mutableStateOf(uiState.member.email) }
  var phoneInput by rememberSaveable { mutableStateOf(uiState.member.phoneNumber ?: "") }
  Spacer(Modifier.height(16.dp))
  val phoneErrorText = uiState.member.phoneNumberErrorMessage?.let { stringResource(id = it) }
  HedvigTextField(
    text = phoneInput,
    onValueChange = onValueChange@{ newInput ->
      if (newInput.any { it.isWhitespace() }) return@onValueChange
      phoneNumberChanged(newInput)
      phoneInput = newInput
    },
    labelText = stringResource(R.string.PHONE_NUMBER_ROW_TITLE),
    errorState = if (phoneErrorText == null) {
      HedvigTextFieldDefaults.ErrorState.NoError
    } else {
      HedvigTextFieldDefaults.ErrorState.Error.WithMessage(phoneErrorText)
    },
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Phone,
      imeAction = ImeAction.Next,
    ),
    textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Large,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  AnimatedVisibility(visible = phoneErrorText != null) {
    Spacer(Modifier.height(4.dp))
  }
  Spacer(Modifier.height(4.dp))
  val emailErrorText = uiState.member.emailErrorMessage?.let { stringResource(id = it) }
  HedvigTextField(
    text = emailInput,
    onValueChange = onValueChange@{ newInput ->
      if (newInput.any { it.isWhitespace() }) return@onValueChange
      emailChanged(newInput)
      emailInput = newInput
    },
    labelText = stringResource(R.string.PROFILE_MY_INFO_EMAIL_LABEL),
    errorState = if (emailErrorText == null) {
      HedvigTextFieldDefaults.ErrorState.NoError
    } else {
      HedvigTextFieldDefaults.ErrorState.Error.WithMessage(emailErrorText)
    },
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Email,
      imeAction = ImeAction.Done,
    ),
    keyboardActions = KeyboardActions(
      onDone = {
        updateEmailAndPhoneNumber()
        focusManager.clearFocus()
      },
    ),
    textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Large,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(16.dp))
  AnimatedVisibility(
    visible = uiState.canSubmit || uiState.isSubmitting,
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    HedvigButton(
      text = stringResource(R.string.general_save_button),
      enabled = uiState.canSubmit,
      onClick = {
        focusManager.clearFocus()
        updateEmailAndPhoneNumber()
      },
      isLoading = uiState.isSubmitting,
      modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewMyInfoScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      MyInfoScreen(
        // uiState = Loading,
        uiState = Success(
          member = MyInfoMember(
            "email@email.com",
            null,
            "072102103",
            null,
          ),
          isSubmitting = false,
          canSubmit = true,
        ),
        updateEmailAndPhoneNumber = {},
        navigateUp = {},
        emailChanged = {},
        phoneNumberChanged = {},
        reload = {},
      )
    }
  }
}
