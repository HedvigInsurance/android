package com.hedvig.android.feature.travelcertificate.ui.overview

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.SUCCESS
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarActionType
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.ui.overview.TravelCertificateOverviewUiState.Success
import hedvig.resources.R
import java.io.File

@Composable
internal fun TravelCertificateOverviewDestination(
  travelCertificateUrl: TravelCertificateUrl,
  viewModel: TravelCertificateOverviewViewModel,
  navigateUp: () -> Unit,
  onShareTravelCertificate: (File) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TravelCertificateOverview(
    travelCertificateUrl = travelCertificateUrl,
    onDownloadCertificate = { viewModel.emit(TravelCertificateOverviewEvent.OnDownloadCertificate(it)) },
    navigateUp = navigateUp,
    onShareTravelCertificate = onShareTravelCertificate,
    uiState = uiState,
    onRetry = {
      viewModel.emit(TravelCertificateOverviewEvent.RetryLoadData)
    },
  )
}

@Composable
internal fun TravelCertificateOverview(
  travelCertificateUrl: TravelCertificateUrl,
  onDownloadCertificate: (TravelCertificateUrl) -> Unit,
  onRetry: () -> Unit,
  navigateUp: () -> Unit,
  onShareTravelCertificate: (File) -> Unit,
  uiState: TravelCertificateOverviewUiState,
) {
  when (uiState) {
    TravelCertificateOverviewUiState.Failure -> {
      HedvigScaffold(
        navigateUp = navigateUp,
        topAppBarActionType = TopAppBarActionType.CLOSE,
      ) {
        HedvigErrorSection(
          onButtonClick = onRetry,
          modifier = Modifier.weight(1f),
        )
      }
    }

    TravelCertificateOverviewUiState.Loading -> {
      HedvigFullScreenCenterAlignedProgress(modifier = Modifier.fillMaxSize())
    }

    is TravelCertificateOverviewUiState.Success -> {
      LaunchedEffect(uiState.travelCertificateUri) {
        uiState.travelCertificateUri?.let {
          onShareTravelCertificate(it)
        }
      }
      HedvigScaffold(
        navigateUp,
        itemsColumnHorizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.weight(1f))
        EmptyState(
          text = stringResource(R.string.CERTIFICATES_EMAIL_SENT),
          description = stringResource(R.string.travel_certificate_travel_certificate_ready_description),
          iconStyle = SUCCESS,
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        HedvigNotificationCard(
          message = stringResource(R.string.travel_certificate_download_recommendation),
          priority = NotificationPriority.Info,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        HedvigButton(
          text = if (uiState.travelCertificateUri != null) {
            stringResource(R.string.travel_certificate_share_travel_certificate)
          } else {
            stringResource(R.string.travel_certificate_download_travel_certificate)
          },
          onClick = {
            if (uiState.travelCertificateUri != null) {
              onShareTravelCertificate(uiState.travelCertificateUri)
            } else {
              onDownloadCertificate(travelCertificateUrl)
            }
          },
          enabled = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        HedvigTextButton(
          text = stringResource(id = R.string.general_done_button),
          onClick = navigateUp,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelCertificateOverview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelCertificateOverview(
        travelCertificateUrl = TravelCertificateUrl(""),
        onDownloadCertificate = {},
        navigateUp = {},
        onShareTravelCertificate = {},
        uiState = Success(null),
        onRetry = {},
      )
    }
  }
}
