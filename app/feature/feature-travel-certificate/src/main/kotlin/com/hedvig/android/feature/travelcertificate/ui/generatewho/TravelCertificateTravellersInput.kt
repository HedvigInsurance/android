package com.hedvig.android.feature.travelcertificate.ui.generatewho

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.material3.onSecondaryContainedButtonContainer
import com.hedvig.android.core.designsystem.material3.secondaryContainedButtonContainer
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.RoundedCornerCheckBox
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.design.system.hedvig.Checkbox
import com.hedvig.android.design.system.hedvig.CheckboxDefaults.CheckboxSize.Large
import com.hedvig.android.design.system.hedvig.CheckboxDefaults.CheckboxStyle
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import hedvig.resources.R

@Composable
internal fun TravelCertificateTravellersInputDestination(
  viewModel: TravelCertificateTravellersInputViewModel,
  navigateUp: () -> Unit,
  onNavigateToOverview: (TravelCertificateUrl) -> Unit,
  onNavigateToCoInsuredAddInfo: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TravelCertificateTravellersInput(
    uiState,
    navigateUp,
    { viewModel.emit(TravelCertificateTravellersInputEvent.RetryLoadData) },
    onNavigateToOverview,
    { viewModel.emit(TravelCertificateTravellersInputEvent.ChangeCoInsuredChecked(it)) },
    { viewModel.emit(TravelCertificateTravellersInputEvent.ChangeMemberChecked) },
    onNavigateToCoInsuredAddInfo,
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
  onNavigateToCoInsuredAddInfo: () -> Unit,
  onGenerateTravelCertificate: () -> Unit,
) {
  Box(Modifier.fillMaxSize()) {
    when (uiState) {
      TravelCertificateTravellersInputUiState.Loading -> HedvigFullScreenCenterAlignedProgress()

      TravelCertificateTravellersInputUiState.Failure -> {
        HedvigScaffold(
          navigateUp = navigateUp,
        ) {
          HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
        }
      }

      is TravelCertificateTravellersInputUiState.UrlFetched -> {
        LaunchedEffect(Unit) {
          onNavigateToOverview(uiState.travelCertificateUrl)
        }
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
          Checkbox(
            optionText = uiState.memberFullName,
            chosenState = if (uiState.isMemberIncluded) Chosen else NotChosen,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            onClick = changeMemberChecked,
            checkboxStyle = CheckboxStyle.Default,
            checkboxSize = Large,
          )
          for (i in uiState.coInsuredList) {
            Spacer(Modifier.height(4.dp))
            com.hedvig.android.design.system.hedvig.HedvigTheme {
              // todo: where do we apply the theme now that we're still on both old and new theme?
              Checkbox(
                optionText = i.name,
                chosenState = if (i.isIncluded) Chosen else NotChosen,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp),
                onClick = { changeCoInsuredChecked(i) },
                checkboxStyle = CheckboxStyle.Default,
                checkboxSize = Large,
              )
            }
          }
          if (uiState.coInsuredHasMissingInfo) {
            Spacer(Modifier.height(16.dp))
            VectorInfoCard(
              text = stringResource(id = R.string.travel_certificate_missing_coinsured_info),
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            ) {
              Button(
                onClick = {
                  onNavigateToCoInsuredAddInfo()
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
          listOf(
            CoInsured("id", "Co-insured Baby", null, null, false),
            CoInsured("id2", "Co-insured Baby 2", null, null, true),
          ),
          "The Member Themselves",
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
        TravelCertificateTravellersInputUiState.Failure,
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
