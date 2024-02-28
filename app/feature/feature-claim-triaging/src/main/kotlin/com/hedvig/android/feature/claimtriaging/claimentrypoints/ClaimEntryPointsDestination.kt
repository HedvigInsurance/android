package com.hedvig.android.feature.claimtriaging.claimentrypoints

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.X
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.core.ui.text.WarningTextWithIcon
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimtriaging.EntryPoint
import com.hedvig.android.data.claimtriaging.EntryPointId
import com.hedvig.android.data.claimtriaging.EntryPointOption
import com.hedvig.android.feature.claimtriaging.OptionChipsFlowRow
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun ClaimEntryPointsDestination(
  viewModel: ClaimEntryPointsViewModel,
  onEntryPointWithOptionsSubmit: (EntryPointId, ImmutableList<EntryPointOption>) -> Unit,
  startClaimFlow: (ClaimFlowStep) -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState.nextStep) {
    val nextStep = uiState.nextStep
    if (nextStep != null) {
      startClaimFlow(nextStep)
    }
  }
  ClaimEntryPointsScreen(
    uiState = uiState,
    onSelectEntryPoint = viewModel::onSelectEntryPoint,
    onContinue = {
      val selectedEntryPoint = uiState.selectedEntryPoint
      if (selectedEntryPoint != null) {
        val entryPointOptions = selectedEntryPoint.entryPointOptions
        if (entryPointOptions.isNullOrEmpty()) {
          viewModel.startClaimFlow()
        } else {
          onEntryPointWithOptionsSubmit(selectedEntryPoint.id, entryPointOptions)
        }
      } else {
        viewModel.continueWithoutSelection()
      }
    },
    showedStartClaimError = viewModel::showedStartClaimError,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  )
}

@Composable
private fun ClaimEntryPointsScreen(
  uiState: ClaimEntryPointsUiState,
  onSelectEntryPoint: (EntryPoint) -> Unit,
  onContinue: () -> Unit,
  showedStartClaimError: () -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  if (uiState.startClaimErrorMessage != null) {
    ErrorDialog(
      title = stringResource(R.string.something_went_wrong),
      message = stringResource(R.string.GENERAL_ERROR_BODY),
      onDismiss = showedStartClaimError,
    )
  }
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarActions = {
      IconButton(
        onClick = closeClaimFlow,
        content = { Icon(imageVector = Icons.Hedvig.X, contentDescription = null) },
      )
    },
    modifier = Modifier.fillMaxWidth(),
  ) {
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(R.string.CLAIMS_TRIAGING_WHAT_HAPPENED_TITLE),
      style = MaterialTheme.typography.headlineMedium,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(32.dp))
    Spacer(Modifier.weight(1f))
    AnimatedVisibility(
      visible = uiState.haveTriedContinuingWithoutSelection,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Column {
        WarningTextWithIcon(
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .wrapContentWidth(),
          text = stringResource(R.string.CLAIMS_SELECT_CATEGORY),
        )
        Spacer(Modifier.height(16.dp))
      }
    }
    OptionChipsFlowRow(
      items = uiState.entryPoints,
      itemDisplayName = EntryPoint::displayName,
      selectedItem = uiState.selectedEntryPoint,
      onItemClick = { entryPoint -> onSelectEntryPoint(entryPoint) },
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigContainedButton(
      text = stringResource(R.string.claims_continue_button),
      onClick = onContinue,
      isLoading = uiState.isLoading,
      enabled = uiState.canContinue,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimEntryPointsScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      val entryPoints = remember {
        List(12) {
          val displayName = buildString { repeat((4..14).random()) { append(('a'..'z').random()) } }
          EntryPoint(EntryPointId(it.toString()), displayName, persistentListOf())
        }.toImmutableList()
      }
      ClaimEntryPointsScreen(
        uiState = ClaimEntryPointsUiState(
          entryPoints = entryPoints,
          selectedEntryPoint = entryPoints[3],
          startClaimErrorMessage = "",
        ),
        onSelectEntryPoint = {},
        onContinue = {},
        showedStartClaimError = {},
        navigateUp = {},
        closeClaimFlow = {},
      )
    }
  }
}
