package com.hedvig.android.feature.terminateinsurance.step.choose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.SelectIndicationCircle
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.terminateinsurance.data.InsuranceEligibleForTermination
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.ui.TerminationOverviewScreenScaffold
import kotlinx.datetime.LocalDate
import octopus.type.CurrencyCode

@Composable
internal fun ChooseInsuranceToTerminateDestination(
  viewModel: ChooseInsuranceToTerminateViewModel,
  navigateUp: () -> Unit,
  navigateToNextStep: (TerminateInsuranceStep) -> Unit,
) {
  val uiState: ChooseInsuranceToTerminateStepUiState by viewModel.uiState.collectAsStateWithLifecycle()
  ChooseInsuranceToTerminateScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    navigateToNextStep = navigateToNextStep,
    selectInsurance = { viewModel.emit(ChooseInsuranceToTerminateEvent.SelectInsurance(it)) },
  )
}

@Composable
internal fun ChooseInsuranceToTerminateScreen(
  uiState: ChooseInsuranceToTerminateStepUiState,
  navigateUp: () -> Unit,
  selectInsurance: (id: String) -> Unit,
  navigateToNextStep: (TerminateInsuranceStep) -> Unit,
) {
  when (uiState) {
    ChooseInsuranceToTerminateStepUiState.Failure -> TODO()
    ChooseInsuranceToTerminateStepUiState.Loading -> TODO()
    is ChooseInsuranceToTerminateStepUiState.Success -> {
      TerminationOverviewScreenScaffold(
        navigateUp = navigateUp,
        topAppBarText = "",
      ) {
        Text(
          text = "Cancellation", // todo: put real string here
          fontSize = MaterialTheme.typography.headlineSmall.fontSize,
          fontStyle = MaterialTheme.typography.headlineSmall.fontStyle,
          fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Text(
          text = "Select the insurance you \nwant to cancel", // todo: put real string here
          fontSize = MaterialTheme.typography.headlineSmall.fontSize,
          fontStyle = MaterialTheme.typography.headlineSmall.fontStyle,
          fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        VectorInfoCard(
          text = "Note that you can only cancel one insurance at the same time", // todo: put real string here
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        for (insurance in uiState.insuranceList) {
          HedvigCard(
            onClick = { selectInsurance(insurance.id) },
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .heightIn(72.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
              Text(
                text = insurance.displayName,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
              )
              Spacer(Modifier.width(8.dp))
              SelectIndicationCircle(uiState.selectedId == insurance.id)
            }
          }
          Spacer(modifier = (Modifier.height(4.dp)))
        }
        Spacer(Modifier.height(16.dp))
        // todo: put real string here
        HedvigContainedButton(
          "Continue",
          enabled = uiState.continueEnabled,
          modifier = Modifier.padding(horizontal = 16.dp),
          onClick = { navigateToNextStep(uiState.nextStep) },
        )
        Spacer(Modifier.height(16.dp))
        Spacer(
          Modifier.padding(
            WindowInsets.safeDrawing
              .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom).asPaddingValues(),
          ),
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewChooseInsuranceToTerminateScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ChooseInsuranceToTerminateScreen(
        ChooseInsuranceToTerminateStepUiState.Success(
          insuranceList = listOf(
            InsuranceEligibleForTermination(
              "1",
              "Homeowner Insurance",
              "Bellmansgatan 19A",
              UiMoney(449.0, currencyCode = CurrencyCode.SEK),
              LocalDate(2024, 7, 27),
            ),
          ),
          nextStep = TerminateInsuranceStep.UnknownStep(),
          true,
          null,
        ),
        {},
        {},
        {},
      )
    }
  }
}
