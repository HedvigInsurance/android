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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.Checkbox
import com.hedvig.android.design.system.hedvig.CheckboxGroup
import com.hedvig.android.design.system.hedvig.CheckboxOption
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputUiState.Failure
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputUiState.Success
import hedvig.resources.Res
import hedvig.resources.GENERAL_SUBMIT
import hedvig.resources.travel_certificate_missing_coinsured_button
import hedvig.resources.travel_certificate_missing_coinsured_info
import hedvig.resources.travel_certificate_who_is_traveling
import org.jetbrains.compose.resources.stringResource

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
          FlowHeading(
            stringResource(Res.string.travel_certificate_who_is_traveling),
            null,
            Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
          Spacer(Modifier.weight(1f))
          Spacer(Modifier.height(16.dp))
          Checkbox(
            option = CheckboxOption(uiState.memberFullName),
            selected = uiState.isMemberIncluded,
            onCheckboxSelected = changeMemberChecked,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(4.dp))
          CheckboxGroup(
            options = uiState.coInsuredList.map { coInsured ->
              RadioOption(RadioOptionId(coInsured.id.value), coInsured.name)
            },
            selectedOptions = uiState.coInsuredList.filter { it.isIncluded }.map { RadioOptionId(it.id.value) },
            onRadioOptionSelected = {
              changeCoInsuredChecked(uiState.coInsuredList.first { it.id.value == it.id.value })
            },
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
          if (uiState.coInsuredHasMissingInfo) {
            Spacer(Modifier.height(16.dp))
            HedvigNotificationCard(
              message = stringResource(Res.string.travel_certificate_missing_coinsured_info),
              priority = NotificationPriority.Info,
              style = InfoCardStyle.Button(
                buttonText = stringResource(Res.string.travel_certificate_missing_coinsured_button),
                onButtonClick = dropUnlessResumed { onNavigateToCoInsuredAddInfo() },
              ),
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            )
          }
          Spacer(Modifier.height(16.dp))
          HedvigButton(
            text = stringResource(Res.string.GENERAL_SUBMIT),
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
            CoInsured(CoInsured.CoInsuredId("id"), "Co-insured Baby", null, null, false),
            CoInsured(CoInsured.CoInsuredId("id2"), "Co-insured Baby 2", null, null, true),
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
