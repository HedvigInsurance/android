package com.hedvig.android.feature.travelcertificate.ui.overview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarActionType
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.infocard.DrawableInfoCard
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUri
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import hedvig.resources.R
import java.io.File

@Composable
internal fun TravelCertificateOverview(
  travelCertificateUrl: TravelCertificateUrl,
  onDownloadCertificate: (TravelCertificateUrl) -> Unit,
  navigateBack: () -> Unit,
  isLoading: Boolean,
  travelCertificateUri: TravelCertificateUri?,
  errorMessage: String?,
  onErrorDialogDismissed: () -> Unit,
  onShareTravelCertificate: (TravelCertificateUri) -> Unit,
) {
  if (errorMessage != null) {
    ErrorDialog(
      title = stringResource(id = R.string.general_error),
      message = errorMessage,
      onDismiss = onErrorDialogDismissed,
    )
  }

  LaunchedEffect(travelCertificateUri) {
    travelCertificateUri?.let {
      onShareTravelCertificate(it)
    }
  }

  if (isLoading) {
    Box(modifier = Modifier.fillMaxSize()) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
  } else {
    HedvigScaffold(
      navigateUp = { navigateBack() },
      topAppBarActionType = TopAppBarActionType.CLOSE,
      modifier = Modifier.clearFocusOnTap(),
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
        text = if (travelCertificateUri != null) {
          stringResource(R.string.travel_certificate_share_travel_certificate)
        } else {
          stringResource(R.string.travel_certificate_download_travel_certificate)
        },
        onClick = {
          if (travelCertificateUri != null) {
            onShareTravelCertificate(travelCertificateUri)
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

@HedvigPreview
@Composable
private fun PreviewTravelCertificateOverview() {
  HedvigTheme {
    TravelCertificateOverview(
      travelCertificateUrl = TravelCertificateUrl(""),
      onDownloadCertificate = {},
      navigateBack = {},
      isLoading = false,
      travelCertificateUri = TravelCertificateUri(File("123")),
      errorMessage = null,
      onErrorDialogDismissed = {},
      onShareTravelCertificate = {},
    )
  }
}
