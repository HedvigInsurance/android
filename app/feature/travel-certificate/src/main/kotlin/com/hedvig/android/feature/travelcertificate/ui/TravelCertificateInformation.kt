package com.hedvig.android.feature.travelcertificate.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigInfoCard
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.error.ErrorDialog
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateResult
import hedvig.resources.R

@Composable
fun TravelCertificateInformation(
  infoSections: List<TravelCertificateResult.TraverlCertificateData.InfoSection>?,
  isLoading: Boolean,
  errorMessage: String?,
  onErrorDialogDismissed: () -> Unit,
  onContinue: () -> Unit,
  navigateBack: () -> Unit,
) {
  if (errorMessage != null) {
    ErrorDialog(
      title = stringResource(id = R.string.general_error),
      message = errorMessage,
      onDismiss = onErrorDialogDismissed,
    )
  }

  if (isLoading) {
    Box(modifier = Modifier.fillMaxSize()) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
  } else {
    HedvigScaffold(
      navigateUp = {
        navigateBack()
      },
      modifier = Modifier.clearFocusOnTap(),
    ) {
      Text(
        text = "Skaffa reseintyg",
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(modifier = Modifier.padding(top = 48.dp))
      infoSections?.map {
        InfoCard(
          title = it.title,
          text = it.body,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.padding(top = 8.dp))
      }
      Spacer(modifier = Modifier.weight(1f))
      LargeContainedButton(
        onClick = onContinue,
        shape = MaterialTheme.shapes.squircle,
        modifier = Modifier.padding(horizontal = 16.dp),
      ) {
        Text(stringResource(R.string.travel_certificate_get_travel_certificate_button))
      }
    }

  }
}

@Composable
internal fun InfoCard(
  title: String,
  text: String,
  modifier: Modifier = Modifier,
) {
  HedvigInfoCard(
    modifier = modifier,
    contentPadding = PaddingValues(12.dp),
  ) {
    Icon(
      imageVector = Icons.Default.Info,
      contentDescription = "info",
      modifier = Modifier
        .padding(top = 2.dp)
        .size(16.dp)
        .padding(1.dp),
      tint = MaterialTheme.colorScheme.infoElement,
    )
    Spacer(modifier = Modifier.padding(start = 8.dp))
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
      )
      Spacer(modifier = Modifier.padding(2.dp))
      Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.secondary,
      )
    }
  }
}
