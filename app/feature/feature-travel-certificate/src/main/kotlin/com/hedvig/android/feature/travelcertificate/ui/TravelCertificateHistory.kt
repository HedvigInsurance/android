package com.hedvig.android.feature.travelcertificate.ui

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigSecondaryContainedButton
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.Info
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.data.travelcertificate.TravelCertificate
import com.hedvig.android.feature.travelcertificate.CertificateHistoryEvent
import com.hedvig.android.feature.travelcertificate.CertificateHistoryViewModel
import com.hedvig.android.feature.travelcertificate.navigation.openWebsite
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun TravelCertificateHistoryDestination(
  viewModel: CertificateHistoryViewModel,
  onContinue: () -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  TravelCertificateHistory(
    reload = { viewModel.emit(CertificateHistoryEvent.RetryLoadReferralData) },
    isLoading = uiState.isLoading,
    historyList = uiState.certificateHistoryList,
    onCertificateClick = { url ->
      openWebsite(context, if (url.isBlank()) Uri.EMPTY else Uri.parse(url))
      // todo: openUrl in the browser here: downloads the file trough browser, but does not show it
    },
    errorMessage = uiState.certificateHistoryErrorMessage?.message,
    onContinue = onContinue,
    onErrorDialogDismissed = { viewModel.emit(CertificateHistoryEvent.OnErrorDialogDismissed) },
    navigateUp = navigateUp,
    onShowBottomSheet = { viewModel.emit(CertificateHistoryEvent.ShowBottomSheet) },
    onDismissBottomSheet = { viewModel.emit(CertificateHistoryEvent.DismissBottomSheet) },
    showBottomSheet = uiState.showInfoBottomSheet,
  )
}

@Composable
fun TravelCertificateHistory(
  reload: () -> Unit,
  isLoading: Boolean,
  historyList: List<TravelCertificate>?,
  onCertificateClick: (String) -> Unit,
  errorMessage: String?,
  onContinue: () -> Unit,
  onErrorDialogDismissed: () -> Unit,
  showBottomSheet: Boolean,
  onShowBottomSheet: () -> Unit,
  onDismissBottomSheet: () -> Unit,
  navigateUp: () -> Unit,
) {
  if (errorMessage != null) {
    ErrorDialog(
      title = stringResource(id = R.string.general_error),
      message = errorMessage,
      onDismiss = onErrorDialogDismissed,
    )
  }

  val referralExplanationSheetState = rememberModalBottomSheetState(true)
  if (showBottomSheet) {
    TravelCertificateInfoBottomSheet(
      onDismiss = onDismissBottomSheet,
      sheetState = referralExplanationSheetState,
    )
  }

  if (isLoading) {
    Box(modifier = Modifier.fillMaxSize()) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
  } else {
    HedvigScaffold(
      navigateUp = navigateUp,
      modifier = Modifier.clearFocusOnTap(),
      topAppBarText = stringResource(id = R.string.PROFILE_ROW_TRAVEL_CERTIFICATE),
      topAppBarActions = {
        IconButton(
          onClick = { onShowBottomSheet() },
          modifier = Modifier.size(40.dp),
        ) {
          Icon(
            imageVector = Icons.Hedvig.Info,
            contentDescription = stringResource(R.string.REFERRALS_INFO_BUTTON_CONTENT_DESCRIPTION),
            modifier = Modifier.size(24.dp),
          )
        }
      },
    ) {
      if (historyList.isNullOrEmpty()) {
        ShowInitialInfo()
      } else {
        ShowCertificatesHistory(
          list = historyList,
          onCertificateClick = onCertificateClick,
        )
      }
      Spacer(modifier = Modifier.weight(1f))
      VectorInfoCard(
        text = String.format(stringResource(id = R.string.travel_certificate_start_date_info), 45),
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp, 16.dp, 16.dp, 8.dp),
      )
      HedvigSecondaryContainedButton(
        text = stringResource(R.string.travel_certificate_get_travel_certificate_button),
        onClick = onContinue,
        modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 0.dp),
      )
      Spacer(Modifier.height(32.dp))
    }
  }
}

@Composable
private fun ShowInitialInfo() {
  Text(
    text = stringResource(id = R.string.travel_certificate_description),
    style = MaterialTheme.typography.headlineSmall,
    textAlign = TextAlign.Center,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
}

@Composable
private fun ShowCertificatesHistory(list: List<TravelCertificate>, onCertificateClick: (String) -> Unit) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    for (i in list) {
      CertificateCard(
        certificate = i,
        onCertificateClick = onCertificateClick,
      )
    }
  }
}

@Composable
private fun CertificateCard(
  certificate: TravelCertificate,
  onCertificateClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = { onCertificateClick(certificate.signedUrl) })
      .padding(16.dp),
  ) {
    val isExpired = isExpired(certificate.expiryDate)
    val color = if (isExpired) Color.Red else Color.Unspecified
    val expiryDateText =
      "${certificate.expiryDate.dayOfMonth} ${certificate.expiryDate.month.name.lowercase().substring(0, 3)}"
    Text(
      text = expiryDateText,
      style = MaterialTheme.typography.bodyLarge,
      color = color,
    )
    Spacer(modifier = Modifier.weight(1f))
    Text(
      text = if (isExpired) {
        stringResource(id = R.string.travel_certificate_expired)
      } else {
        stringResource(id = R.string.travel_certificate_active)
      },
      style = MaterialTheme.typography.bodyLarge,
      color = color,
    )
  }
}

private fun isExpired(expiryDate: LocalDate): Boolean {
  val current = java.time.LocalDate.now()
  val currentYear = current.year
  val currentDay = current.dayOfYear
  val expiryYear = expiryDate.year
  val expiryDay = expiryDate.dayOfYear
  return if (expiryYear >= currentYear) {
    expiryDay <= currentDay
  } else {
    true
  }
}
