package com.hedvig.android.feature.purchase.apartment.ui.sign

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun SigningDestination(
  viewModel: SigningViewModel,
  navigateToSuccess: (startDate: String?) -> Unit,
  navigateToFailure: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val canOpenBankId = remember { canBankIdAppHandleUri(context) }
  var hasNavigated by remember { mutableStateOf(false) }

  LaunchedEffect(uiState) {
    if (hasNavigated) return@LaunchedEffect
    when (val state = uiState) {
      is SigningUiState.Success -> {
        hasNavigated = true
        navigateToSuccess(state.startDate)
      }
      is SigningUiState.Failed -> {
        hasNavigated = true
        navigateToFailure()
      }
      is SigningUiState.Polling -> {}
    }
  }

  when (val state = uiState) {
    is SigningUiState.Polling -> {
      if (canOpenBankId && !state.bankIdOpened) {
        LaunchedEffect(Unit) {
          val bankIdUri = Uri.parse("https://app.bankid.com/?autostarttoken=${state.autoStartToken}&redirect=null")
          context.startActivity(Intent(Intent.ACTION_VIEW, bankIdUri))
          viewModel.emit(SigningEvent.BankIdOpened)
        }
        HedvigFullScreenCenterAlignedProgress()
      } else if (!canOpenBankId) {
        QrCodeSigningScreen(
          liveQrCodeData = state.liveQrCodeData,
          onOpenBankId = {
            val bankIdUri = Uri.parse("https://app.bankid.com/?autostarttoken=${state.autoStartToken}&redirect=null")
            context.startActivity(Intent(Intent.ACTION_VIEW, bankIdUri))
            viewModel.emit(SigningEvent.BankIdOpened)
          },
        )
      } else {
        HedvigFullScreenCenterAlignedProgress()
      }
    }

    is SigningUiState.Success,
    is SigningUiState.Failed,
    -> HedvigFullScreenCenterAlignedProgress()
  }
}

@Composable
private fun QrCodeSigningScreen(liveQrCodeData: String?, onOpenBankId: () -> Unit) {
  HedvigScaffold(navigateUp = {}) {
    Spacer(Modifier.weight(1f))
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    ) {
      HedvigText(
        text = "Logga in med BankID",
        style = HedvigTheme.typography.headlineMedium,
      )
      Spacer(Modifier.height(8.dp))
      HedvigText(
        text = "Skanna QR-koden med BankID-appen på en annan enhet",
        style = HedvigTheme.typography.bodyMedium,
        color = HedvigTheme.colorScheme.textSecondary,
      )
      Spacer(Modifier.height(24.dp))
      if (liveQrCodeData != null) {
        QRCode(
          data = liveQrCodeData,
          modifier = Modifier.size(200.dp),
        )
      } else {
        HedvigFullScreenCenterAlignedProgress()
      }
      Spacer(Modifier.height(24.dp))
      HedvigButton(
        text = "\u00d6ppna BankID",
        onClick = onOpenBankId,
        enabled = true,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    Spacer(Modifier.weight(1f))
  }
}

@Composable
private fun QRCode(data: String, modifier: Modifier = Modifier) {
  var intSize: IntSize? by remember { mutableStateOf(null) }
  val painter by produceState<Painter>(ColorPainter(Color.Transparent), intSize, data) {
    val size = intSize ?: return@produceState
    val bitmapPainter: BitmapPainter = withContext(Dispatchers.Default) {
      val bitMatrix: BitMatrix = QRCodeWriter().encode(
        data,
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
    contentDescription = "BankID QR code",
    modifier.onSizeChanged { intSize = it },
  )
}

@SuppressLint("QueryPermissionsNeeded")
private fun canBankIdAppHandleUri(context: Context): Boolean {
  return try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      context.packageManager.getPackageInfo(
        BANK_ID_APP_PACKAGE_NAME,
        PackageManager.PackageInfoFlags.of(0),
      )
    } else {
      @Suppress("DEPRECATION")
      context.packageManager.getPackageInfo(BANK_ID_APP_PACKAGE_NAME, 0)
    }
    true
  } catch (e: PackageManager.NameNotFoundException) {
    logcat(LogPriority.INFO) { "BankID app not installed, will show QR code" }
    false
  }
}

private const val BANK_ID_APP_PACKAGE_NAME = "com.bankid.bus"
