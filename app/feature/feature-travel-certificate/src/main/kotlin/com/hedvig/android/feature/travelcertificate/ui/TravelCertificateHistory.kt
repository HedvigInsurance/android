package com.hedvig.android.feature.travelcertificate.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigSecondaryContainedButton
import com.hedvig.android.core.designsystem.component.information.HedvigInformationSection
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.Info
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.data.travelcertificate.TravelCertificate
import com.hedvig.android.feature.travelcertificate.CertificateHistoryEvent
import com.hedvig.android.feature.travelcertificate.CertificateHistoryUiState
import com.hedvig.android.feature.travelcertificate.CertificateHistoryViewModel
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUri
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun TravelCertificateHistoryDestination(
  viewModel: CertificateHistoryViewModel,
  onContinue: () -> Unit,
  navigateUp: () -> Unit,
  onShareTravelCertificate: (TravelCertificateUri) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TravelCertificateHistoryScreen(
    reload = { viewModel.emit(CertificateHistoryEvent.RetryLoadReferralData) },
    onCertificateClick = { url ->
      viewModel.emit(CertificateHistoryEvent.DownloadCertificate(url))
    },
    onContinue = onContinue,
    navigateUp = navigateUp,
    onShareTravelCertificate = onShareTravelCertificate,
    uiState = uiState,
  )
}

@Composable
private fun TravelCertificateHistoryScreen(
  reload: () -> Unit,
  onCertificateClick: (String) -> Unit,
  onContinue: () -> Unit,
  navigateUp: () -> Unit,
  onShareTravelCertificate: (TravelCertificateUri) -> Unit,
  uiState: CertificateHistoryUiState,
) {
  var showBottomSheet by remember { mutableStateOf(false) }
  val explanationSheetState = rememberModalBottomSheetState(true)
  if (showBottomSheet) {
    TravelCertificateInfoBottomSheet(
      onDismiss = { showBottomSheet = false },
      sheetState = explanationSheetState,
    )
  }

  when (uiState) {
    CertificateHistoryUiState.FailureDownloadingCertificate -> {
      ErrorDialog(
        title = stringResource(id = R.string.general_error),
        message = stringResource(id = R.string.travel_certificate_downloading_error),
        onDismiss = {
          reload()
        },
      )
    }

    CertificateHistoryUiState.FailureDownloadingHistory -> {
      HedvigScaffold(
        navigateUp = navigateUp,
        modifier = Modifier.clearFocusOnTap(),
        topAppBarText = stringResource(id = R.string.PROFILE_ROW_TRAVEL_CERTIFICATE),
        topAppBarActions = {
          IconButton(
            onClick = { showBottomSheet = true },
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
        Spacer(modifier = Modifier.weight(1f))
        HedvigInformationSection(
          title = stringResource(id = R.string.something_went_wrong),
          buttonText = stringResource(id = R.string.GENERAL_RETRY),
          onButtonClick = {
            reload()
          },
        )
        Spacer(modifier = Modifier.weight(1f))
      }
    }

    CertificateHistoryUiState.Loading -> {
      Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
      }
    }

    is CertificateHistoryUiState.SuccessDownloadingCertificate -> {
      LaunchedEffect(uiState.downLoadingUri) {
        uiState.downLoadingUri?.let {
          onShareTravelCertificate(it)
        }
      }
    }

    is CertificateHistoryUiState.SuccessDownloadingHistory -> {
      ShowHistory(
        onIconClick = { showBottomSheet = true },
        onCertificateClick = onCertificateClick,
        onContinue = onContinue,
        navigateUp = navigateUp,
        historyList = uiState.certificateHistoryList,
      )
    }
  }
}

@Composable
private fun ShowHistory(
  onIconClick: () -> Unit,
  onCertificateClick: (String) -> Unit,
  onContinue: () -> Unit,
  navigateUp: () -> Unit,
  historyList: List<TravelCertificate>,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    modifier = Modifier.clearFocusOnTap(),
    topAppBarText = stringResource(id = R.string.PROFILE_ROW_TRAVEL_CERTIFICATE),
    topAppBarActions = {
      IconButton(
        onClick = { onIconClick() },
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
    if (historyList.isEmpty()) {
      Spacer(modifier = Modifier.weight(1f))
      ShowInitialInfo()
    } else {
      ShowNotEmptyList(
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

@Composable
private fun ShowInitialInfo() {
  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    HedvigInformationSection(
      stringResource(id = R.string.travel_certificate_empty_list_message),
    )
  }
}

@Composable
private fun ShowNotEmptyList(list: List<TravelCertificate>, onCertificateClick: (String) -> Unit) {
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

@HedvigPreview
@Composable
private fun PreviewTravelCertificateHistoryScreenWithEmptyList() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TravelCertificateHistoryScreen(
        {},
        {},
        {},
        {},
        {},
        CertificateHistoryUiState.SuccessDownloadingHistory(listOf()),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelCertificateHistoryScreenWithExpiredEarlier() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TravelCertificateHistoryScreen(
        {},
        {},
        {},
        {},
        {},
        CertificateHistoryUiState.SuccessDownloadingHistory(
          listOf(
            TravelCertificate(
              startDate = LocalDate(2024, 6, 2),
              expiryDate = LocalDate(2024, 7, 9),
              id = "13213",
              signedUrl = "wkehdkwed",
            ),
            TravelCertificate(
              startDate = LocalDate(2024, 1, 6),
              expiryDate = LocalDate(2024, 9, 10),
              id = "13213",
              signedUrl = "wkehdkwed",
            ),
            TravelCertificate(
              startDate = LocalDate(2023, 12, 9),
              expiryDate = LocalDate(2024, 1, 31),
              id = "13213",
              signedUrl = "wkehdkwed",
            ),
          ),
        ),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelCertificateHistoryScreenWithExpiredToday() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TravelCertificateHistoryScreen(
        {},
        {},
        {},
        {},
        {},
        CertificateHistoryUiState.SuccessDownloadingHistory(
          listOf(
            TravelCertificate(
              startDate = LocalDate(2024, 1, 6),
              expiryDate = LocalDate(2024, 9, 10),
              id = "13213",
              signedUrl = "wkehdkwed",
            ),
            TravelCertificate(
              startDate = LocalDate(2023, 11, 25),
              expiryDate = LocalDate(
                java.time.LocalDate.now().year,
                java.time.LocalDate.now().month,
                java.time.LocalDate.now().dayOfMonth,
              ),
              id = "13213",
              signedUrl = "wkehdkwed",
            ),
          ),
        ),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewCertificateHistoryLoading() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TravelCertificateHistoryScreen(
        {},
        {},
        {},
        {},
        {},
        CertificateHistoryUiState.Loading,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewErrorWithHistory() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TravelCertificateHistoryScreen(
        {},
        {},
        {},
        {},
        {},
        CertificateHistoryUiState.FailureDownloadingHistory,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewErrorWithCertificate() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TravelCertificateHistoryScreen(
        {},
        {},
        {},
        {},
        {},
        CertificateHistoryUiState.FailureDownloadingCertificate,
      )
    }
  }
}
