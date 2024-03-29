package com.hedvig.android.feature.travelcertificate.ui.overview

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigSecondaryContainedButton
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.small.hedvig.Checkmark
import com.hedvig.android.core.ui.appbar.m3.TopAppBarActionType
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
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
  )
}

@Composable
internal fun TravelCertificateOverview(
  travelCertificateUrl: TravelCertificateUrl,
  onDownloadCertificate: (TravelCertificateUrl) -> Unit,
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
          onButtonClick = { onDownloadCertificate(travelCertificateUrl) },
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
        Spacer(Modifier.height(32.dp))
        Spacer(modifier = Modifier.weight(1f))
        Icon(
          imageVector = Icons.Hedvig.Checkmark,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.typeElement,
        )
        Spacer(Modifier.height(16.dp))
        Text(
          text = stringResource(id = R.string.travel_certificate_travel_certificate_ready),
          textAlign = TextAlign.Center,
          style = LocalTextStyle.current.copy(
            lineBreak = LineBreak.Heading,
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
          text = stringResource(id = R.string.travel_certificate_travel_certificate_ready_description),
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(32.dp))
        Spacer(modifier = Modifier.weight(1f))
        VectorInfoCard(
          text = stringResource(id = R.string.travel_certificate_download_recommendation),
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
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
            .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        HedvigSecondaryContainedButton(
          text = stringResource(id = R.string.general_done_button),
          onClick = navigateUp,
          modifier = Modifier
            .padding(horizontal = 16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = MaterialTheme.colorScheme.primary,
          ),
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
    TravelCertificateOverview(
      travelCertificateUrl = TravelCertificateUrl(""),
      onDownloadCertificate = {},
      navigateUp = {},
      onShareTravelCertificate = {},
      uiState = TravelCertificateOverviewUiState.Success(null),
    )
  }
}
