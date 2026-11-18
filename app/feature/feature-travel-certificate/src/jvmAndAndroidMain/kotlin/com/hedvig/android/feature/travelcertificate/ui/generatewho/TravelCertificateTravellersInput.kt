package com.hedvig.android.feature.travelcertificate.ui.generatewho

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.compose.ui.NavigationEffect
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
import com.hedvig.android.feature.travelcertificate.ui.generatewho.CoInsured.CoInsuredId
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputUiState.Failure
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputUiState.Success
import hedvig.resources.GENERAL_SUBMIT
import hedvig.resources.Res
import hedvig.resources.something_went_wrong
import hedvig.resources.travel_certificate_missing_coinsured_button
import hedvig.resources.travel_certificate_missing_coinsured_info
import hedvig.resources.travel_certificate_who_is_traveling
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelCertificateTravellersInputDestination(
  viewModel: TravelCertificateTravellersInputViewModel,
  navigateUp: () -> Unit,
  onNavigateToCoInsuredAddInfo: () -> Unit,
  onCertificateCreated: (TravelCertificateUrl) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TravellersNavigationEffect(uiState = uiState, onCertificateCreated = onCertificateCreated)
  TravelCertificateTravellersInput(
    uiState = uiState,
    navigateUp = navigateUp,
    reload = { viewModel.emit(TravelCertificateTravellersInputEvent.RetryLoadData) },
    setCoInsuredIncluded = { id, included ->
      viewModel.emit(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(id, included))
    },
    setMemberIncluded = { viewModel.emit(TravelCertificateTravellersInputEvent.SetMemberIncluded(it)) },
    onNavigateToCoInsuredAddInfo = onNavigateToCoInsuredAddInfo,
    onGenerateTravelCertificate = { viewModel.emit(TravelCertificateTravellersInputEvent.GenerateTravelCertificate) },
  )
}

/** Leaves for the certificate on [TravelCertificateTravellersInputUiState.Created]. */
@Composable
internal fun TravellersNavigationEffect(
  uiState: TravelCertificateTravellersInputUiState,
  onCertificateCreated: (TravelCertificateUrl) -> Unit,
) {
  NavigationEffect((uiState as? TravelCertificateTravellersInputUiState.Created)?.exit, onCertificateCreated)
}

@Composable
private fun TravelCertificateTravellersInput(
  uiState: TravelCertificateTravellersInputUiState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  setCoInsuredIncluded: (CoInsuredId, Boolean) -> Unit,
  setMemberIncluded: (Boolean) -> Unit,
  onNavigateToCoInsuredAddInfo: () -> Unit,
  onGenerateTravelCertificate: () -> Unit,
) {
  Box(Modifier.fillMaxSize()) {
    when (uiState) {
      TravelCertificateTravellersInputUiState.Loading,
      is TravelCertificateTravellersInputUiState.Created,
      -> {
        HedvigFullScreenCenterAlignedProgress()
      }

      Failure -> {
        HedvigScaffold(
          navigateUp = navigateUp,
        ) {
          HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
        }
      }

      is Success -> {
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
            onCheckboxSelected = { setMemberIncluded(!uiState.isMemberIncluded) },
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(4.dp))
          CheckboxGroup(
            options = uiState.coInsuredList.map { coInsured ->
              RadioOption(
                id = RadioOptionId(id = coInsured.id.value),
                text = coInsured.name,
              )
            },
            selectedOptions = uiState.coInsuredList.filter { it.isIncluded }.map { RadioOptionId(it.id.value) },
            onRadioOptionSelected = { radioOptionId ->
              val coInsured = uiState.coInsuredList.first { radioOptionId.id == it.id.value }
              setCoInsuredIncluded(coInsured.id, !coInsured.isIncluded)
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
          if (uiState.createFailed) {
            Spacer(Modifier.height(16.dp))
            HedvigNotificationCard(
              message = stringResource(Res.string.something_went_wrong),
              priority = NotificationPriority.Error,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            )
          }
          Spacer(Modifier.height(16.dp))
          HedvigButton(
            text = stringResource(Res.string.GENERAL_SUBMIT),
            onClick = onGenerateTravelCertificate,
            isLoading = uiState.isCreating,
            enabled = uiState.canCreate,
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
        previewContent,
        {},
        {},
        { _, _ -> },
        {},
        {},
        {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelCertificateTravellersInputWithCreateFailure() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelCertificateTravellersInput(
        previewContent.copy(isCreating = false, createFailed = true),
        {},
        {},
        { _, _ -> },
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
        { _, _ -> },
        {},
        {},
        {},
      )
    }
  }
}

private val previewContent = Success(
  memberFullName = "The Member Themselves",
  isMemberIncluded = true,
  coInsuredList = listOf(
    CoInsured(CoInsured.CoInsuredId("id"), "Co-insured Baby", null, null, false),
    CoInsured(CoInsured.CoInsuredId("id2"), "Co-insured Baby 2", null, null, true),
  ),
  coInsuredHasMissingInfo = true,
  isCreating = true,
  createFailed = false,
)
