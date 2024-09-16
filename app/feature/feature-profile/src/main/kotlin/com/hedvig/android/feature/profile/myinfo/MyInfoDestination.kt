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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.hedvig.android.core.designsystem.animation.FadeAnimatedContent
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
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
        Column(Modifier.fillMaxSize()) {
          when (animatedUiState) {
            MyInfoUiState.Loading -> {
              HedvigFullScreenCenterAlignedProgressDebounced()
            }

            MyInfoUiState.Error -> {
              Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                HedvigErrorSection(onButtonClick = reload)
              }
            }

            is MyInfoUiState.Success -> {
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
  uiState: MyInfoUiState.Success,
  phoneNumberChanged: (String) -> Unit,
  emailChanged: (String) -> Unit,
  updateEmailAndPhoneNumber: () -> Unit,
  focusManager: FocusManager,
) {
  var emailInput by rememberSaveable { mutableStateOf(uiState.member.email) }
  var phoneInput by rememberSaveable { mutableStateOf(uiState.member.phoneNumber ?: "") }
  Spacer(Modifier.height(16.dp))
  val errorText = uiState.member.phoneNumberErrorMessage?.let { stringResource(id = it) }
  HedvigTextField(
    value = phoneInput,
    onValueChange = onValueChange@{ newInput ->
      if (newInput.any { it.isWhitespace() }) return@onValueChange
      phoneNumberChanged(newInput)
      phoneInput = newInput
    },
    label = { Text(stringResource(R.string.PHONE_NUMBER_ROW_TITLE)) },
    errorText = errorText,
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Phone,
      imeAction = ImeAction.Next,
    ),
    withNewDesign = true,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  AnimatedVisibility(visible = errorText != null) {
    Spacer(Modifier.height(4.dp))
  }
  Spacer(Modifier.height(4.dp))
  HedvigTextField(
    value = emailInput,
    onValueChange = onValueChange@{ newInput ->
      if (newInput.any { it.isWhitespace() }) return@onValueChange
      emailChanged(newInput)
      emailInput = newInput
    },
    label = { Text(stringResource(R.string.PROFILE_MY_INFO_EMAIL_LABEL)) },
    errorText = uiState.member.emailErrorMessage?.let { stringResource(id = it) },
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
    withNewDesign = true,
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
    HedvigContainedButton(
      text = stringResource(R.string.general_save_button),
      enabled = uiState.canSubmit,
      onClick = {
        focusManager.clearFocus()
        updateEmailAndPhoneNumber()
      },
      isLoading = uiState.isSubmitting,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewMyInfoScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      MyInfoScreen(
        uiState = MyInfoUiState.Success(
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
