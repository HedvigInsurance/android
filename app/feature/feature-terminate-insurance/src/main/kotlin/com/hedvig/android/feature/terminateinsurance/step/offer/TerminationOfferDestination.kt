package com.hedvig.android.feature.terminateinsurance.step.offer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.terminateinsurance.data.OfferAction
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold

@Composable
internal fun TerminationOfferDestination(
  viewModel: TerminationOfferViewModel,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  onCtaClick: (OfferAction) -> Unit,
  onNavigateToNextStep: (TerminateInsuranceStep) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState) {
    val state = uiState as? TerminationOfferUiState.Content ?: return@LaunchedEffect
    if (state.nextStep != null) {
      viewModel.emit(TerminationOfferEvent.ClearNextStep)
      onNavigateToNextStep(state.nextStep)
    }
  }
  TerminationOfferScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
    onCtaClick = onCtaClick,
    onSkipClick = { viewModel.emit(TerminationOfferEvent.Skip) },
    onRetry = { viewModel.emit(TerminationOfferEvent.Skip) },
  )
}

@Composable
private fun TerminationOfferScreen(
  uiState: TerminationOfferUiState,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  onCtaClick: (OfferAction) -> Unit,
  onSkipClick: () -> Unit,
  onRetry: () -> Unit,
) {
  when (uiState) {
    TerminationOfferUiState.Error -> {
      HedvigScaffold(navigateUp = navigateUp) {
        HedvigErrorSection(
          onButtonClick = onRetry,
          modifier = Modifier.weight(1f),
        )
      }
    }

    is TerminationOfferUiState.Content -> {
      TerminationOfferContentScreen(
        uiState = uiState,
        navigateUp = navigateUp,
        closeTerminationFlow = closeTerminationFlow,
        onCtaClick = { onCtaClick(uiState.action) },
        onSkipClick = onSkipClick,
      )
    }
  }
}

@Composable
private fun TerminationOfferContentScreen(
  uiState: TerminationOfferUiState.Content,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  onCtaClick: () -> Unit,
  onSkipClick: () -> Unit,
) {
  TerminationScaffold(
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
  ) { _ ->
    FlowHeading(
      title = uiState.title,
      description = null,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    HedvigNotificationCard(
      message = uiState.description,
      priority = NotificationDefaults.NotificationPriority.Campaign,
      withIcon = true,
      style = NotificationDefaults.InfoCardStyle.Button(
        buttonText = uiState.buttonTitle,
        onButtonClick = onCtaClick,
      ),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigTextButton(
      text = uiState.skipButtonTitle,
      isLoading = uiState.skipLoading,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      onClick = onSkipClick,
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigShortMultiScreenPreview
@Composable
private fun PreviewTerminationOfferScreen(
  @PreviewParameter(OfferUiStateProvider::class) uiState: TerminationOfferUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TerminationOfferScreen(
        uiState = uiState,
        navigateUp = {},
        closeTerminationFlow = {},
        onCtaClick = {},
        onSkipClick = {},
        onRetry = {},
      )
    }
  }
}

private class OfferUiStateProvider :
  CollectionPreviewParameterProvider<TerminationOfferUiState>(
    listOf(
      TerminationOfferUiState.Content(
        title = "Erbjudande för dig",
        description = "Vill du se vilket pris du får hos Hedvig? Väljer du att försäkra ditt nya hem får du 20% rabatt de första 6 månaderna",
        buttonTitle = "Få ett prisförslag",
        skipButtonTitle = "Hoppa över",
        action = OfferAction.UPDATE_ADDRESS,
      ),
      TerminationOfferUiState.Content(
        title = "Erbjudande för dig",
        description = "Vill du se vilket pris du får hos Hedvig?",
        buttonTitle = "Få ett prisförslag",
        skipButtonTitle = "Hoppa över",
        action = OfferAction.UPDATE_ADDRESS,
        skipLoading = true,
      ),
      TerminationOfferUiState.Error,
    ),
  )
