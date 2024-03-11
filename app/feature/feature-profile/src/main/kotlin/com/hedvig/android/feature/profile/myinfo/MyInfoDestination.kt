package com.hedvig.android.feature.profile.myinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import hedvig.resources.R

@Composable
internal fun MyInfoDestination(viewModel: MyInfoViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  MyInfoScreen(
    uiState = uiState,
    emailChanged = viewModel::emailChanged,
    phoneNumberChanged = viewModel::phoneNumberChanged,
    updateEmailAndPhoneNumber = viewModel::updateEmailAndPhoneNumber,
    dismissError = viewModel::dismissError,
    navigateUp = navigateUp,
  )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MyInfoScreen(
  uiState: MyInfoUiState,
  updateEmailAndPhoneNumber: () -> Unit,
  navigateUp: () -> Unit,
  emailChanged: (String) -> Unit,
  phoneNumberChanged: (String) -> Unit,
  dismissError: () -> Unit,
) {
  val localSoftwareKeyboardController = LocalSoftwareKeyboardController.current
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
      ) { uiState ->
        Column(Modifier.fillMaxSize()) {
          when {
            uiState.isLoading -> {
              HedvigFullScreenCenterAlignedProgressDebounced()
            }
            uiState.errorMessage != null -> {
              Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                HedvigErrorSection(onButtonClick = dismissError)
              }
            }
            else -> {
              Spacer(Modifier.height(16.dp))
              val errorText = uiState.member?.phoneNumber?.errorMessageRes?.let { stringResource(id = it) }
              HedvigTextField(
                value = uiState.member?.phoneNumber?.input ?: "",
                onValueChange = { newInput ->
                  if (newInput.indices.all { newInput[it].isWhitespace().not() }) {
                    phoneNumberChanged(newInput)
                  }
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
                value = uiState.member?.email?.input ?: "",
                onValueChange = { newInput ->
                  if (newInput.indices.all { newInput[it].isWhitespace().not() }) {
                    emailChanged(newInput)
                  }
                },
                label = { Text(stringResource(R.string.PROFILE_MY_INFO_EMAIL_LABEL)) },
                errorText = uiState.member?.email?.errorMessageRes?.let { stringResource(id = it) },
                keyboardOptions = KeyboardOptions(
                  keyboardType = KeyboardType.Email,
                  imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                  onDone = {
                    updateEmailAndPhoneNumber()
                    localSoftwareKeyboardController?.hide()
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
                    updateEmailAndPhoneNumber()
                    localSoftwareKeyboardController?.hide()
                  },
                  isLoading = uiState.isSubmitting,
                  modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(16.dp))
              }
            }
          }
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewMyInfoScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      MyInfoScreen(
        uiState = MyInfoUiState(
          member = MyInfoMember(
            ValidatedInput("email@email.com"),
            ValidatedInput("072102103"),
          ),
          isLoading = false,
        ),
        updateEmailAndPhoneNumber = {},
        navigateUp = {},
        emailChanged = {},
        phoneNumberChanged = {},
        dismissError = {},
      )
    }
  }
}
