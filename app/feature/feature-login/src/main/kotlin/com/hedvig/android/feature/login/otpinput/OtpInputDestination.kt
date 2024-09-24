package com.hedvig.android.feature.login.otpinput

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Reload
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive

@Composable
fun OtpInputDestination(
  viewModel: OtpInputViewModel,
  navigateUp: () -> Unit,
  onNavigateToLoggedIn: () -> Unit,
  onOpenEmailApp: () -> Unit,
) {
  var showResentMessageNotification by remember { mutableStateOf(false) }
  LaunchedEffect(viewModel) {
    viewModel.events.collectLatest { event ->
      when (event) {
        is OtpInputViewModel.Event.Success -> onNavigateToLoggedIn()
        OtpInputViewModel.Event.CodeResent -> {
          try {
            delay(1.seconds)
            showResentMessageNotification = true
            delay(1.seconds)
          } finally {
            showResentMessageNotification = false
          }
        }
      }
    }
  }
  val viewState by viewModel.viewState.collectAsStateWithLifecycle()
  OtpInputScreen(
    onInputChanged = viewModel::setInput,
    onOpenEmailApp = onOpenEmailApp,
    onSubmitCode = viewModel::submitCode,
    onResendCode = viewModel::resendCode,
    navigateUp = navigateUp,
    inputValue = viewState.input,
    credential = viewState.credential,
    networkErrorMessage = viewState.networkErrorMessage,
    loadingResend = viewState.loadingResend,
    loadingCode = viewState.loadingCode,
    showResentMessageNotification = showResentMessageNotification,
  )
}

@Composable
fun OtpInputScreen(
  onInputChanged: (String) -> Unit,
  onOpenEmailApp: () -> Unit,
  onSubmitCode: (String) -> Unit,
  onResendCode: () -> Unit,
  navigateUp: () -> Unit,
  inputValue: String,
  credential: String,
  networkErrorMessage: String?,
  loadingResend: Boolean,
  loadingCode: Boolean,
  showResentMessageNotification: Boolean,
  modifier: Modifier = Modifier,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(hedvig.resources.R.string.login_navigation_bar_center_element_title),
    modifier = modifier.fillMaxSize(),
  ) {
    Box(
      Modifier
        .fillMaxSize()
        .weight(1f),
      propagateMinConstraints = true,
    ) {
      OtpInputScreenContents(
        credential,
        inputValue,
        onInputChanged,
        onSubmitCode,
        networkErrorMessage,
        onResendCode,
        loadingResend,
        onOpenEmailApp,
        showResentMessageNotification,
      )
      if (loadingCode) {
        HedvigFullScreenCenterAlignedProgress(show = loadingCode)
      }
    }
  }
}

@Composable
private fun OtpInputScreenContents(
  credential: String,
  inputValue: String,
  onInputChanged: (String) -> Unit,
  onSubmitCode: (String) -> Unit,
  otpErrorMessage: String?,
  onResendCode: () -> Unit,
  loadingResend: Boolean,
  onOpenEmailApp: () -> Unit,
  showResentMessageNotification: Boolean,
  modifier: Modifier = Modifier,
) {
  val keyboardController = LocalSoftwareKeyboardController.current
  Column(
    modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .verticalScroll(rememberScrollState()),
  ) {
    Spacer(Modifier.height(60.dp))
    HedvigText(
      text = stringResource(hedvig.resources.R.string.login_title_check_your_email),
      style = HedvigTheme.typography.headlineMedium,
    )
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = stringResource(hedvig.resources.R.string.login_subtitle_verification_code_email, credential),
      style = HedvigTheme.typography.bodyLarge,
    )
    Spacer(Modifier.height(40.dp))
    SixDigitCodeInputField(inputValue, onInputChanged, keyboardController, onSubmitCode, otpErrorMessage)
    Spacer(Modifier.height(16.dp))
    ResendCodeItem(onResendCode, keyboardController, loadingResend, Modifier.align(Alignment.CenterHorizontally))
    if (showResentMessageNotification) {
      Spacer(Modifier.height(16.dp))
      HedvigText(
        text = stringResource(hedvig.resources.R.string.login_snackbar_code_resent),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(hedvig.resources.R.string.login_open_email_app_button),
      onClick = onOpenEmailApp,
      enabled = true,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun SixDigitCodeInputField(
  inputValue: String,
  onInputChanged: (String) -> Unit,
  keyboardController: SoftwareKeyboardController?,
  onSubmitCode: (String) -> Unit,
  otpErrorMessage: String?,
) {
  HedvigTextField(
    modifier = Modifier.fillMaxWidth(),
    text = inputValue,
    labelText = "######",
    textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
    onValueChange = { newValue ->
      if (newValue.length <= 6 && newValue.isDigitsOnly()) {
        onInputChanged(newValue)
      }

      if (newValue.length == 6) {
        keyboardController?.hide()
        onSubmitCode(newValue)
      }
    },
    errorState = if (otpErrorMessage != null) {
      HedvigTextFieldDefaults.ErrorState.Error.WithMessage(otpErrorMessage)
    } else {
      HedvigTextFieldDefaults.ErrorState.NoError
    },
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Number,
    ),
  )
}

@Composable
private fun ResendCodeItem(
  onResendCode: () -> Unit,
  keyboardController: SoftwareKeyboardController?,
  loadingResend: Boolean,
  modifier: Modifier = Modifier,
) {
  HedvigButton(
    onClick = {
      onResendCode()
      keyboardController?.hide()
    },
    enabled = !loadingResend,
    buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
    buttonSize = ButtonDefaults.ButtonSize.Large,
    modifier = modifier.fillMaxWidth(),
  ) {
    RotatingIcon(loadingResend)
    Spacer(modifier = Modifier.width(8.dp))
    HedvigText(
      modifier = Modifier.align(Alignment.CenterVertically),
      text = stringResource(hedvig.resources.R.string.login_smedium_button_active_resend_code),
      style = HedvigTheme.typography.bodySmall,
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun RotatingIcon(isLoading: Boolean) {
  val angle = remember { Animatable(0f) }
  LaunchedEffect(angle, isLoading) {
    if (isLoading) {
      while (isActive) {
        angle.animateTo(
          360f,
          spring(stiffness = Spring.StiffnessVeryLow / 2, visibilityThreshold = 0.1f),
        )
        angle.snapTo(0f)
      }
    } else {
      if (angle.value != 0f) {
        // Finish the rotation if it was already ongoing.
        angle.animateTo(360f)
        angle.snapTo(0f)
      }
      angle.snapTo(0f)
    }
  }
  Icon(
    imageVector = HedvigIcons.Reload,
    modifier = Modifier.graphicsLayer { rotationZ = angle.value },
    contentDescription = stringResource(hedvig.resources.R.string.login_smedium_button_active_resend_code),
  )
}

@HedvigPreview
@Composable
private fun PreviewOtpInputScreenValid() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      OtpInputScreen(
        onInputChanged = {},
        onOpenEmailApp = {},
        onSubmitCode = {},
        onResendCode = {},
        navigateUp = {},
        inputValue = "0123456",
        credential = "john@doe.com",
        networkErrorMessage = null,
        loadingResend = false,
        loadingCode = false,
        showResentMessageNotification = true,
      )
    }
  }
}
