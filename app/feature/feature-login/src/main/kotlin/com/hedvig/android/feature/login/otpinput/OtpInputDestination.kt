package com.hedvig.android.feature.login.otpinput

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.RestartTwoArrows
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive

@Composable
fun OtpInputDestination(
  viewModel: OtpInputViewModel,
  navigateUp: () -> Unit,
  startLoggedInActivity: () -> Unit,
  onOpenEmailApp: () -> Unit,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val snackbarResendMessage = stringResource(hedvig.resources.R.string.login_snackbar_code_resent)
  LaunchedEffect(viewModel) {
    viewModel.events.collectLatest { event ->
      when (event) {
        is OtpInputViewModel.Event.Success -> startLoggedInActivity()
        OtpInputViewModel.Event.CodeResent -> {
          delay(1.seconds)
          snackbarHostState.showSnackbar(snackbarResendMessage)
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
    snackbarHostState = snackbarHostState,
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
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    topBar = {
      TopAppBarWithBack(
        onClick = navigateUp,
        title = stringResource(hedvig.resources.R.string.login_navigation_bar_center_element_title),
      )
    },
    snackbarHost = {
      SnackbarHost(snackbarHostState)
    },
    modifier = modifier.fillMaxSize(),
  ) { paddingValues ->
    Box(Modifier.fillMaxSize(), propagateMinConstraints = true) {
      OtpInputScreenContents(
        credential,
        inputValue,
        onInputChanged,
        onSubmitCode,
        networkErrorMessage,
        onResendCode,
        loadingResend,
        onOpenEmailApp,
        Modifier.padding(paddingValues),
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
    Text(
      text = stringResource(hedvig.resources.R.string.login_title_check_your_email),
      style = MaterialTheme.typography.headlineMedium,
    )
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(hedvig.resources.R.string.login_subtitle_verification_code_email, credential),
      style = MaterialTheme.typography.bodyLarge,
    )
    Spacer(Modifier.height(40.dp))
    SixDigitCodeInputField(inputValue, onInputChanged, keyboardController, onSubmitCode, otpErrorMessage)
    Spacer(
      // 20 from design, 6 to account for TextButton extra space taken
      Modifier.height((20 - 6).dp),
    )
    ResendCodeItem(onResendCode, keyboardController, loadingResend, Modifier.align(Alignment.CenterHorizontally))
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(hedvig.resources.R.string.login_open_email_app_button),
      onClick = onOpenEmailApp,
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun ColumnScope.SixDigitCodeInputField(
  inputValue: String,
  onInputChanged: (String) -> Unit,
  keyboardController: SoftwareKeyboardController?,
  onSubmitCode: (String) -> Unit,
  otpErrorMessage: String?,
) {
  OutlinedTextField(
    modifier = Modifier.fillMaxSize(),
    value = inputValue,
    onValueChange = { newValue ->
      if (newValue.length <= 6 && newValue.isDigitsOnly()) {
        onInputChanged(newValue)
      }

      if (newValue.length == 6) {
        keyboardController?.hide()
        onSubmitCode(newValue)
      }
    },
    isError = otpErrorMessage != null,
    shape = MaterialTheme.shapes.medium,
    textStyle = LocalTextStyle.current.copy(
      letterSpacing = 20.sp,
      fontWeight = FontWeight(400),
      fontSize = TextUnit(28f, TextUnitType.Sp),
      textAlign = TextAlign.Center,
    ),
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Number,
    ),
  )
  AnimatedVisibility(otpErrorMessage != null) {
    Column {
      Spacer(Modifier.height(8.dp))
      Text(
        text = otpErrorMessage ?: "",
        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun ResendCodeItem(
  onResendCode: () -> Unit,
  keyboardController: SoftwareKeyboardController?,
  loadingResend: Boolean,
  modifier: Modifier = Modifier,
) {
  TextButton(
    onClick = {
      onResendCode()
      keyboardController?.hide()
    },
    enabled = !loadingResend,
    modifier = modifier,
  ) {
    RotatingIcon(loadingResend)
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      modifier = Modifier.align(Alignment.CenterVertically),
      text = stringResource(hedvig.resources.R.string.login_smedium_button_active_resend_code),
      style = MaterialTheme.typography.bodySmall,
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
    imageVector = Icons.Hedvig.RestartTwoArrows,
    modifier = Modifier.graphicsLayer { rotationZ = angle.value },
    contentDescription = stringResource(hedvig.resources.R.string.login_smedium_button_active_resend_code),
  )
}

@HedvigPreview
@Composable
private fun PreviewOtpInputScreenValid() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
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
        loadingCode = true,
        snackbarHostState = SnackbarHostState(),
      )
    }
  }
}
