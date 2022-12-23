@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalUnitApi::class)

package com.hedvig.app.feature.genericauth.otpinput

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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.FullScreenProgressOverlay
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.app.R
import kotlinx.coroutines.isActive

@Composable
fun OtpInputScreen(
  onInputChanged: (String) -> Unit,
  onOpenExternalApp: () -> Unit,
  onSubmitCode: (String) -> Unit,
  onResendCode: () -> Unit,
  onBackPressed: () -> Unit,
  inputValue: String,
  credential: String,
  networkErrorMessage: String?,
  loadingResend: Boolean,
  loadingCode: Boolean,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    Scaffold(
      scaffoldState = remember { ScaffoldState(drawerState, snackbarHostState) },
      topBar = {
        TopAppBarWithBack(
          onClick = onBackPressed,
          title = stringResource(hedvig.resources.R.string.login_navigation_bar_center_element_title),
        )
      },
      modifier = modifier
        .fillMaxSize()
        .safeDrawingPadding(),
    ) { paddingValues ->
      OtpInputScreenContents(
        credential,
        inputValue,
        onInputChanged,
        onSubmitCode,
        networkErrorMessage,
        onResendCode,
        loadingResend,
        onOpenExternalApp,
        Modifier.padding(paddingValues),
      )
    }
    FullScreenProgressOverlay(show = loadingCode)
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
  onOpenExternalApp: () -> Unit,
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
      style = MaterialTheme.typography.h4,
    )
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(hedvig.resources.R.string.login_subtitle_verification_code_email, credential),
      style = MaterialTheme.typography.body1,
    )
    Spacer(Modifier.height(40.dp))
    SixDigitCodeInputField(inputValue, onInputChanged, keyboardController, onSubmitCode, otpErrorMessage)
    Spacer(
      Modifier.height((20 - 6).dp), // 20 from design, 6 to account for TextButton extra space taken
    )
    ResendCodeItem(onResendCode, keyboardController, loadingResend, Modifier.align(Alignment.CenterHorizontally))
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    LargeContainedButton(onOpenExternalApp) {
      Text(stringResource(hedvig.resources.R.string.login_open_email_app_button))
    }
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
      letterSpacing = TextUnit(20f, TextUnitType.Sp),
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
        style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.error),
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
      style = MaterialTheme.typography.caption,
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
      }
      angle.snapTo(0f)
    }
  }
  Icon(
    modifier = Modifier.graphicsLayer { rotationZ = angle.value },
    painter = painterResource(id = R.drawable.ic_refresh),
    contentDescription = stringResource(hedvig.resources.R.string.login_smedium_button_active_resend_code),
  )
}

@Preview(showBackground = true)
@Composable
fun OtpInputScreenValidPreview() {
  HedvigTheme {
    OtpInputScreen(
      onInputChanged = {},
      onOpenExternalApp = {},
      onSubmitCode = {},
      onResendCode = {},
      onBackPressed = {},
      inputValue = "0123456",
      credential = "john@doe.com",
      networkErrorMessage = null,
      loadingResend = false,
      loadingCode = false,
      snackbarHostState = SnackbarHostState(),
    )
  }
}

@Preview(showBackground = true)
@Composable
fun OtpInputScreenInvalidPreview() {
  HedvigTheme {
    OtpInputScreen(
      onInputChanged = {},
      onOpenExternalApp = {},
      onSubmitCode = {},
      onResendCode = {},
      onBackPressed = {},
      inputValue = "0123456",
      credential = "john@doe.com",
      networkErrorMessage = null,
      loadingResend = false,
      loadingCode = false,
      snackbarHostState = SnackbarHostState(),
    )
  }
}
