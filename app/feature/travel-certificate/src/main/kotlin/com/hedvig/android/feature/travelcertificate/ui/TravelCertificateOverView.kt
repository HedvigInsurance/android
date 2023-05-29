package com.hedvig.android.feature.travelcertificate.ui

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
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.error.ErrorDialog
import com.hedvig.android.core.ui.infocard.DrawableInfoCard
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUri
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import hedvig.resources.R


@Composable
fun TravelCertificateOverView(
  travelCertificateUrl: TravelCertificateUrl,
  onDownloadCertificate: (TravelCertificateUrl) -> Unit,
  navigateBack: () -> Unit,
  isLoading: Boolean,
  travelCertificateUri: TravelCertificateUri?,
  errorMessage: String?,
  onErrorDialogDismissed: () -> Unit,
  onSuccess: (TravelCertificateUri) -> Unit,
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
      onSuccess(it)
    }
  }

  if (isLoading) {
    Box(modifier = Modifier.fillMaxSize()) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
  } else {
    HedvigScaffold(
      navigateUp = { navigateBack() },
      modifier = Modifier.clearFocusOnTap(),
    ) {
      Spacer(modifier = Modifier.padding(top = 38.dp))
      Text(
        text = "Ha en trevlig resa!",
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(modifier = Modifier.padding(top = 58.dp))
      DrawableInfoCard(
        title = "Ditt reseintyg är nu klart",
        text = "Vi rekommenderar att du skriver ut intyget så du lätt kan ha med dig det överallt.",
        icon = painterResource(id = com.hedvig.android.core.designsystem.R.drawable.ic_checkmark_success),
        iconColor = MaterialTheme.colorScheme.primary,
        colors = CardDefaults.outlinedCardColors(
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(modifier = Modifier.padding(top = 8.dp))
      VectorInfoCard(
        title = "Ditt reseintyg är nu klart",
        text = "Vi har skickat reseintyg till din e-mail. Du kan även ladda ner beviset till din telefon via knappen nedan.",
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(modifier = Modifier.weight(1f))
      LargeContainedButton(
        onClick = { onDownloadCertificate(travelCertificateUrl) },
        shape = MaterialTheme.shapes.squircle,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .padding(bottom = 32.dp),
      ) {
        Text("Ladda ner reseintyg")
      }
    }
  }
}

@HedvigPreview
@Composable
fun TravelCertificateOverView() {
  HedvigTheme {
    TravelCertificateOverView(
      travelCertificateUrl = TravelCertificateUrl(""),
      onDownloadCertificate = {},
      navigateBack = {},
      isLoading = false,
      travelCertificateUri = TravelCertificateUri(""),
      errorMessage = null,
      onErrorDialogDismissed = {},
      onSuccess = {},
    )
  }
}
