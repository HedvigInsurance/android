package com.hedvig.android.feature.change.tier.ui.stepstart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.INFO
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedLinearProgress
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import com.hedvig.android.feature.change.tier.ui.stepstart.FailureReason.GENERAL
import com.hedvig.android.feature.change.tier.ui.stepstart.FailureReason.QUOTES_ARE_EMPTY
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeState.Failure
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeState.Loading
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeState.Success
import hedvig.resources.Res
import hedvig.resources.DASHBOARD_OPEN_CHAT
import hedvig.resources.TERMINATION_FLOW_I_UNDERSTAND_TEXT
import hedvig.resources.TERMINATION_NO_TIER_QUOTES_SUBTITLE
import hedvig.resources.TIER_FLOW_PROCESSING
import hedvig.resources.general_close_button
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StartChangeTierFlowDestination(
  viewModel: StartTierFlowViewModel,
  popBackStack: () -> Unit,
  launchFlow: (InsuranceCustomizationParameters) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState: StartTierChangeState by viewModel.uiState.collectAsStateWithLifecycle()
  StartChangeTierFlowScreen(
    uiState = uiState,
    popBackStack = popBackStack,
    reload = {
      viewModel.emit(StartTierChangeEvent.Reload)
    },
    launchFlow = launchFlow,
    onNavigateToNewConversation = onNavigateToNewConversation,
    navigateUp = navigateUp,
  )
}

@Composable
private fun StartChangeTierFlowScreen(
  uiState: StartTierChangeState,
  popBackStack: () -> Unit,
  reload: () -> Unit,
  launchFlow: (InsuranceCustomizationParameters) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateUp: () -> Unit,
) {
  when (uiState) {
    is Failure -> {
      FailureScreen(
        reload = reload,
        popBackStack = popBackStack,
        reason = uiState.reason,
      )
    }

    Loading -> HedvigFullScreenCenterAlignedLinearProgress(
      title = stringResource(Res.string.TIER_FLOW_PROCESSING),
    )

    is Success -> {
      LaunchedEffect(uiState.paramsToNavigate) {
        val params = uiState.paramsToNavigate
        launchFlow(params)
      }
    }

    is StartTierChangeState.Deflect -> DeflectScreen(
      title = uiState.title,
      message = uiState.message,
      closeFlow = popBackStack,
      onNavigateToNewConversation = onNavigateToNewConversation,
      navigateUp = navigateUp,
    )
  }
}

@Composable
internal fun DeflectScreen(
  title: String,
  message: String,
  closeFlow: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    modifier = modifier,
  ) {
    FlowHeading(
      title = title,
      description = null,
      modifier = Modifier.padding(horizontal = 16.dp),
    )

    Spacer(Modifier.height(16.dp))
    HedvigText(
      message,
      color = HedvigTheme.colorScheme.textSecondaryTranslucent,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      stringResource(Res.string.TERMINATION_FLOW_I_UNDERSTAND_TEXT),
      enabled = true,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      onClick = closeFlow,
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.DASHBOARD_OPEN_CHAT),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    ) {
      onNavigateToNewConversation()
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun FailureScreen(reload: () -> Unit, popBackStack: () -> Unit, reason: FailureReason) {
  Box(Modifier.fillMaxSize()) {
    when (reason) {
      GENERAL -> {
        HedvigErrorSection(
          onButtonClick = reload,
          modifier = Modifier.fillMaxSize(),
        )
      }

      QUOTES_ARE_EMPTY -> {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .windowInsetsPadding(
              WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal +
                  WindowInsetsSides.Bottom,
              ),
            ),
        ) {
          Spacer(Modifier.weight(1f))
          EmptyState(
            text = stringResource(Res.string.TERMINATION_NO_TIER_QUOTES_SUBTITLE),
            iconStyle = INFO,
            buttonStyle = NoButton,
            description = null,
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(Modifier.weight(1f))
          HedvigTextButton(
            stringResource(Res.string.general_close_button),
            onClick = popBackStack,
            buttonSize = Large,
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(Modifier.height(32.dp))
        }
      }
    }
  }
}

@HedvigMultiScreenPreview
@Composable
private fun StartTierFlowScreenPreview(
  @PreviewParameter(StartTierChangeStateProvider::class) state: StartTierChangeState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      StartChangeTierFlowScreen(
        uiState = state,
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

internal class StartTierChangeStateProvider :
  CollectionPreviewParameterProvider<StartTierChangeState>(
    listOf(
      Loading,
      Success(
        InsuranceCustomizationParameters(
          "",
          LocalDate(2024, 11, 11),
          listOf("id", "id2"),
        ),
      ),
      Failure(GENERAL),
      Failure(QUOTES_ARE_EMPTY),
      StartTierChangeState.Deflect(
        "How to change back to your previous coverage",
        "To update your coverage, your car first needs to be registered as active with Transportstyrelsen. " +
          "Once that’s done, your insurance will be updated automatically.",
      ),
    ),
  )
