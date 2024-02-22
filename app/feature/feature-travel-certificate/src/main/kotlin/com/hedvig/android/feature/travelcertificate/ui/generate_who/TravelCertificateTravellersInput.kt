package com.hedvig.android.feature.travelcertificate.ui.generate_who

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.material3.onSecondaryContainedButtonContainer
import com.hedvig.android.core.designsystem.material3.secondaryContainedButtonContainer
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.small.hedvig.Checkmark
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import hedvig.resources.R

@Composable
internal fun TravelCertificateTravellersInputDestination(
  viewModel: TravelCertificateTravellersInputViewModel,
  navigateUp: () -> Unit,
  onNavigateToOverview: (TravelCertificateUrl) -> Unit,
  onNavigateToCoEnsuredAddInfo: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TravelCertificateTravellersInput(
    uiState,
    navigateUp,
    { viewModel.emit(TravelCertificateTravellersInputEvent.RetryLoadData) },
    onNavigateToOverview,
    { viewModel.emit(TravelCertificateTravellersInputEvent.ChangeCoEnsuredChecked(it)) },
    { viewModel.emit(TravelCertificateTravellersInputEvent.ChangeMemberChecked) },
    onNavigateToCoEnsuredAddInfo,
    { viewModel.emit(TravelCertificateTravellersInputEvent.GenerateTravelCertificate) },
  )
}

@Composable
private fun TravelCertificateTravellersInput(
  uiState: TravelCertificateTravellersInputUiState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  onNavigateToOverview: (TravelCertificateUrl) -> Unit,
  changeCoInsuredChecked: (CoInsured) -> Unit,
  changeMemberChecked: () -> Unit,
  onNavigateToCoEnsuredAddInfo: () -> Unit,
  onGenerateTravelCertificate: () -> Unit,
) {
  when (uiState) {
    TravelCertificateTravellersInputUiState.Loading -> HedvigFullScreenCenterAlignedProgress()

    TravelCertificateTravellersInputUiState.FailureWithInvalidEmail -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(
          retry = navigateUp,
          modifier = Modifier.weight(1f),
          title = stringResource(id = R.string.travel_certificate_invalid_email),
          subTitle = null,
        )
      }
    }

    TravelCertificateTravellersInputUiState.Failure -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(retry = reload, modifier = Modifier.weight(1f))
      }
    }

    is TravelCertificateTravellersInputUiState.UrlFetched -> {
      onNavigateToOverview(uiState.travelCertificateUrl)
    }

    is TravelCertificateTravellersInputUiState.Success -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        Spacer(Modifier.height(24.dp))
        Text(
          text = stringResource(id = R.string.travel_certificate_who_is_traveling),
          style = MaterialTheme.typography.headlineMedium,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))

        HedvigCard(
          onClick = changeMemberChecked,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .heightIn(72.dp)
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          ) {
            Text(
              text = uiState.memberFullName,
              style = MaterialTheme.typography.headlineSmall,
              modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            RoundedCornerCheckBox(
              isChecked = uiState.isMemberIncluded,
            ) { changeMemberChecked() }
          }
        }
        for (i in uiState.coEnsuredList) {
          Spacer(Modifier.height(4.dp))
          HedvigCard(
            onClick = { changeCoInsuredChecked(i) },
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .heightIn(72.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            ) {
              Text(
                text = i.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
              )
              Spacer(Modifier.width(8.dp))
              RoundedCornerCheckBox(isChecked = i.isIncluded, onCheckedChange = { changeCoInsuredChecked(i) })
            }
          }
        }
        if (uiState.coEnsuredHasMissingInfo) {
          Spacer(Modifier.height(16.dp))
          VectorInfoCard(
            text = stringResource(id = R.string.travel_certificate_missing_coinsured_info),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          ) {
            Button(
              onClick = {
                onNavigateToCoEnsuredAddInfo()
              },
              enabled = true,
              modifier = Modifier.fillMaxWidth(),
              shape = MaterialTheme.shapes.squircleMedium,
              contentPadding = PaddingValues(6.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainedButtonContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainedButtonContainer,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
              ),
            ) {
              Text(
                text = stringResource(id = R.string.travel_certificate_missing_coinsured_button),
                style = MaterialTheme.typography.titleSmall,
              )
            }
          }
        }

        Spacer(Modifier.height(16.dp))
        HedvigContainedButton(
          onClick = {
            if (uiState.hasAtLeastOneTraveler) {
              onGenerateTravelCertificate()
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        ) {
          Text(
            text = stringResource(id = R.string.GENERAL_SUBMIT),
            style = MaterialTheme.typography.bodyLarge,
          )
        }
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun RoundedCornerCheckBox(isChecked: Boolean, onCheckedChange: ((Boolean) -> Unit)?) {
  val checkMarkColor = MaterialTheme.colorScheme.onPrimary
  val checkColor = MaterialTheme.colorScheme.primary
  val uncheckedColor = MaterialTheme.colorScheme.outlineVariant

  Box(
    modifier = Modifier
      .size(24.dp)
      .background(
        if (isChecked) checkColor else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
      )
      .border(
        1.dp,
        if (isChecked) checkColor else uncheckedColor,
        shape = RoundedCornerShape(8.dp),
      )
      .clickable {
        if (onCheckedChange != null) {
          onCheckedChange(isChecked)
        }
      },
    contentAlignment = Alignment.Center,
  ) {
    if (isChecked) {
      Icon(Icons.Hedvig.Checkmark, contentDescription = null, tint = checkMarkColor)
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewRoundedCornerCheckBoxChecked() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      RoundedCornerCheckBox(true, {})
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewRoundedCornerCheckBoxUnChecked() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      RoundedCornerCheckBox(false, {})
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelCertificateTravellersInput() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TravelCertificateTravellersInput(
        TravelCertificateTravellersInputUiState.Success(
          true,
          listOf(CoInsured("id", "Coensured Baby", null, null, false)),
          "The Member Themselves",
          true,
          true,
        ),
        {},
        {},
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelCertificateTravellersInputWithEmailFailure() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TravelCertificateTravellersInput(
        TravelCertificateTravellersInputUiState.FailureWithInvalidEmail,
        {},
        {},
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}
