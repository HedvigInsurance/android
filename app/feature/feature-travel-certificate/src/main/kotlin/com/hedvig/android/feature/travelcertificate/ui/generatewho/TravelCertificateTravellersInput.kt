package com.hedvig.android.feature.travelcertificate.ui.generatewho

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hedvig.android.design.system.hedvig.Checkbox
import com.hedvig.android.design.system.hedvig.CheckboxDefaults.CheckboxSize.Large
import com.hedvig.android.design.system.hedvig.CheckboxDefaults.CheckboxStyle
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputUiState.Failure
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputUiState.Success
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
    uiState = uiState,
    navigateUp = navigateUp,
    reload = { viewModel.emit(TravelCertificateTravellersInputEvent.RetryLoadData) },
    onNavigateToOverview = onNavigateToOverview,
    changeCoInsuredChecked = { viewModel.emit(TravelCertificateTravellersInputEvent.ChangeCoInsuredChecked(it)) },
    changeMemberChecked = { viewModel.emit(TravelCertificateTravellersInputEvent.ChangeMemberChecked) },
    onNavigateToCoInsuredAddInfo = onNavigateToCoInsuredAddInfo,
    onGenerateTravelCertificate = { viewModel.emit(TravelCertificateTravellersInputEvent.GenerateTravelCertificate) },
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
          Spacer(Modifier.height(8.dp))
          HedvigText(
            text = stringResource(id = R.string.travel_certificate_who_is_traveling),
            style = HedvigTheme.typography.headlineMedium,
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
          for (coInsured in uiState.coInsuredList) {
            Spacer(Modifier.height(4.dp))

            Checkbox(
              optionText = coInsured.name,
              chosenState = if (coInsured.isIncluded) Chosen else NotChosen,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
              onClick = { changeCoInsuredChecked(coInsured) },
              checkboxStyle = CheckboxStyle.Default,
              checkboxSize = Large,
            )
          }
          if (uiState.coInsuredHasMissingInfo) {
            Spacer(Modifier.height(16.dp))
            HedvigNotificationCard(
              message = stringResource(R.string.travel_certificate_missing_coinsured_info),
              priority = NotificationPriority.Info,
              style = InfoCardStyle.Button(
                buttonText = stringResource(R.string.travel_certificate_missing_coinsured_button),
                onButtonClick = dropUnlessResumed { onNavigateToCoInsuredAddInfo() },
              ),
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            )
          }
          Spacer(Modifier.height(16.dp))
          HedvigButton(
            text = stringResource(R.string.GENERAL_SUBMIT),
            onClick = {
              if (uiState.hasAtLeastOneTraveler) {
                onGenerateTravelCertificate()
              }
            },
            isLoading = uiState.isButtonLoading,
            enabled = uiState.hasAtLeastOneTraveler,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(16.dp))
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelCertificateTravellersInput() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelCertificateTravellersInput(
        Success(
          true,
          listOf(
            CoInsured("id", "Co-insured Baby", null, null, false),
            CoInsured("id2", "Co-insured Baby 2", null, null, true),
          ),
          "The Member Themselves",
          true,
          isButtonLoading = true,
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
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelCertificateTravellersInput(
        Failure,
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
