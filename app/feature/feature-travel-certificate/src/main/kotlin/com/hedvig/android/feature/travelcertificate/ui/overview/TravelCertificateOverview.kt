package com.hedvig.android.feature.travelcertificate.ui.overview

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarActionType
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.infocard.DrawableInfoCard
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUri
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import hedvig.resources.R

@Composable
internal fun TravelCertificateOverviewDestination(
  travelCertificateUrl: TravelCertificateUrl,
  viewModel: TravelCertificateOverviewViewModel,
  navigateUp: () -> Unit,
  onShareTravelCertificate: (TravelCertificateUri) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TravelCertificateOverview(
    travelCertificateUrl = travelCertificateUrl,
    onDownloadCertificate = { viewModel.emit(TravelCertificateOverviewEvent.OnDownloadCertificate(it)) },
    navigateUp = navigateUp,
    onShareTravelCertificate = onShareTravelCertificate,
    uiState = uiState,
  )
}

@Composable
internal fun TravelCertificateOverview(
  travelCertificateUrl: TravelCertificateUrl,
  onDownloadCertificate: (TravelCertificateUrl) -> Unit,
  navigateUp: () -> Unit,
  onShareTravelCertificate: (TravelCertificateUri) -> Unit,
  uiState: TravelCertificateOverviewUiState,
) {
  when (uiState) {
    TravelCertificateOverviewUiState.Failure -> {
      HedvigScaffold(
        navigateUp = navigateUp,
        topAppBarActionType = TopAppBarActionType.CLOSE,
        modifier = Modifier.clearFocusOnTap(),
      ) {
        HedvigErrorSection(retry = { onDownloadCertificate(travelCertificateUrl) }, modifier = Modifier.weight(1f))
      }
    }

    TravelCertificateOverviewUiState.Loading -> {
      HedvigFullScreenCenterAlignedProgress()
    }

    is TravelCertificateOverviewUiState.Success -> {
      LaunchedEffect(uiState.travelCertificateUri) {
        uiState.travelCertificateUri?.let {
          onShareTravelCertificate(it)
        }
      }
      HedvigScaffold(
        navigateUp = navigateUp,
        topAppBarActionType = TopAppBarActionType.CLOSE,
      ) {
        Spacer(modifier = Modifier.padding(top = 38.dp))
        Text(
          text = stringResource(id = R.string.travel_certificate_have_a_nice_tripe),
          style = MaterialTheme.typography.headlineSmall,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.padding(top = 58.dp))
        DrawableInfoCard(
          title = stringResource(id = R.string.travel_certificate_travel_certificate_ready),
          text = stringResource(id = R.string.travel_certificate_travel_certificate_ready_description),
          icon = painterResource(id = com.hedvig.android.core.design.system.R.drawable.ic_checkmark_success),
          iconColor = MaterialTheme.colorScheme.primary,
          colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
          ),
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        VectorInfoCard(
          text = stringResource(id = R.string.travel_certificate_download_recommendation),
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.padding(top = 8.dp))
        HedvigContainedButton(
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
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelCertificateOverview() {
  HedvigTheme {
    TravelCertificateOverview(
      travelCertificateUrl = TravelCertificateUrl(""),
      onDownloadCertificate = {},
      navigateUp = {},
      onShareTravelCertificate = {},
      uiState = TravelCertificateOverviewUiState.Success(null),
    )
  }
}
