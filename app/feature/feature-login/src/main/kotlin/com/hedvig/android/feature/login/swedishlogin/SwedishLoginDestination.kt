package com.hedvig.android.feature.login.swedishlogin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
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
  onNavigateToLoggedIn: () -> Unit,
) {
  val uiState by swedishLoginViewModel.uiState.collectAsStateWithLifecycle()
  val navigateToLoginScreen = uiState.navigateToLoginScreen
  LaunchedEffect(navigateToLoginScreen) {
    if (!navigateToLoginScreen) return@LaunchedEffect
    onNavigateToLoggedIn()
  }
  SwedishLoginScreen(
    uiState = uiState.bankIdUiState,
    navigateUp = navigateUp,
    loginWithEmail = navigateToEmailLogin,
    enterDemoMode = { swedishLoginViewModel.emit(SwedishLoginEvent.StartDemoMode) },
    didOpenBankId = { swedishLoginViewModel.emit(SwedishLoginEvent.DidOpenBankIDApp) },
    retry = { swedishLoginViewModel.emit(SwedishLoginEvent.Retry) },
  )
}

@Composable
private fun SwedishLoginScreen(
  uiState: BankIdUiState,
  navigateUp: () -> Unit,
  loginWithEmail: () -> Unit,
  enterDemoMode: () -> Unit,
  didOpenBankId: () -> Unit,
  retry: () -> Unit,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(R.string.SETTINGS_LOGIN_ROW),
  ) {
    var showStartDemoDialog by remember { mutableStateOf(false) }
    if (showStartDemoDialog) {
      HedvigAlertDialog(
        title = "${stringResource(R.string.DEMO_MODE_START)}?",
        text = null,
        onDismissRequest = { showStartDemoDialog = false },
        onConfirmClick = enterDemoMode,
      )
    }
    val demoModeModifier = remember(uiState) {
      Modifier
        .testTag("qr_code")
        .pointerInput(Unit) {
          awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val longPress: PointerInputChange? = awaitLongPressOrCancellation(down.id)
            if (longPress != null) {
              showStartDemoDialog = true
            }
          }
        }
    }
    when (uiState) {
      BankIdUiState.StartLoginAttemptFailed -> {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        ) {
          Column {
            HedvigErrorSection(onButtonClick = retry, subTitle = null)
            Box(
              Modifier
                .size(48.dp)
                .then(demoModeModifier),
            )
          }
        }
      }

      is BankIdUiState.BankIdError -> {
        Box(
          Modifier
            .weight(1f)
            .fillMaxWidth(),
          Alignment.Center,
        ) {
          Column {
            HedvigErrorSection(
              title = stringResource(R.string.general_error),
              subTitle = uiState.message,
              onButtonClick = retry,
            )
            Box(
              Modifier
                .size(48.dp)
                .then(demoModeModifier),
            )
          }
        }
      }

      is BankIdUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgress(
          Modifier
            .weight(1f)
            .fillMaxWidth(),
        )
      }

      is BankIdUiState.HandlingBankId -> {
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
          QRCode(
            autoStartToken = uiState.bankIdLiveQrCodeData,
            modifier = Modifier
              .size(180.dp)
              .then(demoModeModifier),
          )
          Spacer(Modifier.height(32.dp))
          HedvigText(
            text = stringResource(R.string.AUTHENTICATION_BANKID_LOGIN_TITLE),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
          HedvigText(
            text = stringResource(R.string.AUTHENTICATION_BANKID_LOGIN_LABEL),
            textAlign = TextAlign.Center,
            color = HedvigTheme.colorScheme.textSecondary,
            modifier = Modifier.fillMaxWidth(),
          )
        }
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.weight(1f))
        if (bankIdState.canOpenBankId) {
          HedvigButton(
            text = stringResource(R.string.AUTHENTICATION_BANKID_OPEN_BUTTON),
            onClick = bankIdState::tryOpenBankId,
            enabled = true,
            buttonStyle = ButtonDefaults.ButtonStyle.Primary,
            buttonSize = ButtonDefaults.ButtonSize.Large,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(8.dp))
        }
        HedvigTextButton(
          text = stringResource(R.string.bankid_missing_login_email_button),
          onClick = loginWithEmail,
          buttonSize = ButtonDefaults.ButtonSize.Large,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
      }

      is BankIdUiState.LoggedIn -> {
        HedvigFullScreenCenterAlignedProgress(Modifier.weight(1f))
      }
    }
  }
}

@Composable
internal fun QRCode(
  autoStartToken: BankIdUiState.HandlingBankId.BankIdLiveQrCodeData?,
  modifier: Modifier = Modifier,
) {
  var intSize: IntSize? by remember { mutableStateOf(null) }
  val painter by produceState<Painter>(ColorPainter(Color.Transparent), intSize, autoStartToken) {
    val size = intSize
    if (size == null || autoStartToken == null) {
      value = ColorPainter(Color.Transparent)
      return@produceState
    }
    val bitmapPainter: BitmapPainter = withContext(Dispatchers.Default) {
      val bitMatrix: BitMatrix = QRCodeWriter().encode(
        autoStartToken.data,
        BarcodeFormat.QR_CODE,
        size.width,
        size.height,
      )
      val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.RGB_565)
      for (x in 0 until size.width) {
        for (y in 0 until size.height) {
          val color = if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
          bitmap.setPixel(x, y, color)
        }
      }
      BitmapPainter(bitmap.asImageBitmap())
    }
    value = bitmapPainter
  }
  Image(
    painter,
    null,
    modifier.onSizeChanged { intSize = it },
  )
}

@Composable
private fun rememberBankIdState(autoStartToken: BankIdUiState.HandlingBankId.AutoStartToken): BankIdState {
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
  val autoStartToken: BankIdUiState.HandlingBankId.AutoStartToken,
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

@HedvigPreview
@Composable
private fun Preview(
  @PreviewParameter(BankIdUiStateProvider::class) uiState: BankIdUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SwedishLoginScreen(
        uiState,
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class BankIdUiStateProvider : CollectionPreviewParameterProvider<BankIdUiState>(
  listOf(
    BankIdUiState.HandlingBankId("", BankIdUiState.HandlingBankId.AutoStartToken(""), null, false, false),
    BankIdUiState.BankIdError("BankIdError"),
    BankIdUiState.StartLoginAttemptFailed,
    BankIdUiState.Loading,
    BankIdUiState.LoggedIn,
  ),
)
