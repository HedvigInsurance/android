package com.hedvig.android.feature.login.swedishlogin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.encoder.QRCode
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import hedvig.resources.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun SwedishLoginDestination(
  swedishLoginViewModel: SwedishLoginViewModel,
  navigateUp: () -> Unit,
  navigateToEmailLogin: () -> Unit,
  startLoggedInActivity: () -> Unit,
) {
  val uiState by swedishLoginViewModel.uiState.collectAsStateWithLifecycle()
  SwedishLoginScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    loginWithEmail = navigateToEmailLogin,
    startLoggedInActivity = {
      swedishLoginViewModel.emit(SwedishLoginEvent.DidNavigateToLoginScreen)
      startLoggedInActivity()
    },
    didOpenBankId = {
      swedishLoginViewModel.emit(SwedishLoginEvent.DidOpenBankIDApp)
    },
    retry = { swedishLoginViewModel.emit(SwedishLoginEvent.Retry) },
  )
}

@Composable
private fun SwedishLoginScreen(
  uiState: SwedishLoginUiState,
  navigateUp: () -> Unit,
  loginWithEmail: () -> Unit,
  startLoggedInActivity: () -> Unit,
  didOpenBankId: () -> Unit,
  retry: () -> Unit,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(R.string.SETTINGS_LOGIN_ROW),
  ) {
    when (uiState) {
      is SwedishLoginUiState.StartLoginAttemptFailed -> {
        Box(
          Modifier.weight(1f).fillMaxWidth(),
          Alignment.Center,
        ) {
          HedvigErrorSection(retry = retry, subTitle = null, withDefaultVerticalSpacing = false)
        }
      }
      is SwedishLoginUiState.BankIdError -> {
        Box(
          Modifier.weight(1f).fillMaxWidth(),
          Alignment.Center,
        ) {
          HedvigErrorSection(
            title = stringResource(R.string.general_error),
            subTitle = uiState.message,
            retry = retry,
            withDefaultVerticalSpacing = false,
          )
        }
      }
      SwedishLoginUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgress(Modifier.weight(1f).fillMaxWidth())
      }
      is SwedishLoginUiState.HandlingBankId -> {
        val navigateToLoginScreen = uiState.navigateToLoginScreen
        LaunchedEffect(navigateToLoginScreen) {
          if (!navigateToLoginScreen) return@LaunchedEffect
          startLoggedInActivity()
        }

        val bankIdState = rememberBankIdState(uiState.autoStartToken)
        val allowOpeningBankId = uiState.allowOpeningBankId
        LaunchedEffect(allowOpeningBankId) {
          if (!allowOpeningBankId) return@LaunchedEffect
          didOpenBankId()
          bankIdState.tryOpenBankId()
        }

        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.weight(1f))
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(horizontal = 16.dp),
        ) {
          QRCode(uiState.autoStartToken, Modifier.size(180.dp))
          Spacer(Modifier.height(32.dp))
          Text(
            text = stringResource(R.string.AUTHENTICATION_BANKID_LOGIN_TITLE),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
          Text(
            text = stringResource(R.string.AUTHENTICATION_BANKID_LOGIN_LABEL),
            textAlign = TextAlign.Center,
            style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = Modifier.fillMaxWidth(),
          )
        }
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.weight(1f))
        if (bankIdState.canOpenBankId) {
          HedvigContainedButton(
            text = stringResource(R.string.AUTHENTICATION_BANKID_OPEN_BUTTON),
            onClick = bankIdState::tryOpenBankId,
            modifier = Modifier.padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(8.dp))
        }
        HedvigTextButton(
          text = stringResource(R.string.bankid_missing_login_email_button),
          onClick = loginWithEmail,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun rememberBankIdState(
  autoStartToken: SwedishLoginUiState.HandlingBankId.AutoStartToken,
): BankIdState {
  val context = LocalContext.current
  return remember(context, autoStartToken) {
    BankIdStateImpl(autoStartToken, context).also {
      it.initialize()
    }
  }
}

@Stable
private interface BankIdState {
  val canOpenBankId: Boolean
  fun tryOpenBankId()
}

@Stable
private class BankIdStateImpl(
  val autoStartToken: SwedishLoginUiState.HandlingBankId.AutoStartToken,
  val context: Context,
) : BankIdState {
  override var canOpenBankId: Boolean by mutableStateOf(false)

  override fun tryOpenBankId() {
    if (!canOpenBankId) {
      logcat(LogPriority.INFO) { "BankID not found, showing QR code instead" }
      return
    }
    logcat(LogPriority.INFO) { "Opened BankID to handle login" }
    context.startActivity(Intent(Intent.ACTION_VIEW, autoStartToken.bankIdUri))
  }

  fun initialize() {
    canOpenBankId = context.canOpenUri(autoStartToken.bankIdUri)
  }

  @SuppressLint("QueryPermissionsNeeded")
  private fun Context.canOpenUri(uri: Uri) = Intent(Intent.ACTION_VIEW, uri).resolveActivity(packageManager) != null
}
