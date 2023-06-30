package com.hedvig.android.feature.claimtriaging.claimgroups

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.error.ErrorDialog
import com.hedvig.android.core.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.ui.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimtriaging.ClaimGroup
import com.hedvig.android.data.claimtriaging.ClaimGroupId
import com.hedvig.android.feature.claimtriaging.OptionChipsFlowRow
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun ClaimGroupsDestination(
  viewModel: ClaimGroupsViewModel,
  onClaimGroupWithEntryPointsSubmit: (ClaimGroup) -> Unit,
  startClaimFlow: (ClaimFlowStep) -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState.nextStep) {
    val nextStep = uiState.nextStep
    if (nextStep != null) {
      startClaimFlow(nextStep)
    }
  }
  ClaimGroupsScreen(
    uiState = uiState,
    loadClaimGroups = viewModel::loadClaimGroups,
    onSelectClaimGroup = viewModel::onSelectClaimGroup,
    onContinue = {
      uiState.selectedClaimGroup?.let { claimGroup ->
        if (claimGroup.entryPoints.isEmpty()) {
          viewModel.startClaimFlow()
        } else {
          onClaimGroupWithEntryPointsSubmit(claimGroup)
        }
      }
    },
    showedStartClaimError = viewModel::showedStartClaimError,
    navigateUp = navigateUp,
  )
}

@Composable
private fun ClaimGroupsScreen(
  uiState: ClaimGroupsUiState,
  loadClaimGroups: () -> Unit,
  onSelectClaimGroup: (claimGroup: ClaimGroup) -> Unit,
  onContinue: () -> Unit,
  showedStartClaimError: () -> Unit,
  navigateUp: () -> Unit,
) {
  if (uiState.startClaimErrorMessage != null) {
    ErrorDialog(
      title = stringResource(hedvig.resources.R.string.something_went_wrong),
      message = stringResource(hedvig.resources.R.string.GENERAL_ERROR_BODY),
      onDismiss = showedStartClaimError,
    )
  }
  HedvigTheme(useNewColorScheme = true) {
    Box(modifier = Modifier.fillMaxSize(), propagateMinConstraints = true) {
      HedvigScaffold(navigateUp = navigateUp) {
        Spacer(Modifier.height(16.dp))
        Text(
          text = stringResource(hedvig.resources.R.string.CLAIM_TRIAGING_NAVIGATION_TITLE),
          style = MaterialTheme.typography.headlineMedium,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(32.dp))
        Spacer(Modifier.weight(1f))
        if (uiState.chipLoadingErrorMessage != null) {
          Text(
            text = stringResource(hedvig.resources.R.string.something_went_wrong),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(16.dp))
          Text(
            text = stringResource(hedvig.resources.R.string.GENERAL_ERROR_BODY),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(24.dp))
          HedvigContainedButton(
            text = stringResource(hedvig.resources.R.string.GENERAL_RETRY),
            onClick = loadClaimGroups,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          )
        } else {
          OptionChipsFlowRow(
            items = uiState.claimGroups,
            itemDisplayName = ClaimGroup::displayName,
            selectedItem = uiState.selectedClaimGroup,
            onItemClick = { claimGroup -> onSelectClaimGroup(claimGroup) },
            modifier = Modifier.padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(8.dp))
          HedvigContainedButton(
            text = stringResource(hedvig.resources.R.string.claims_continue_button),
            onClick = onContinue,
            enabled = uiState.canContinue,
            modifier = Modifier.padding(horizontal = 16.dp),
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
      HedvigFullScreenCenterAlignedProgress(show = uiState.isLoading)
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimGroupsScreen(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) hasError: Boolean,
) {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      val claimGroups = remember {
        List(12) {
          val displayName = buildString { repeat((4..14).random()) { append(('a'..'z').random()) } }
          ClaimGroup(ClaimGroupId(it.toString()), displayName, persistentListOf())
        }.toImmutableList()
      }
      ClaimGroupsScreen(
        uiState = ClaimGroupsUiState(
          claimGroups = claimGroups,
          selectedClaimGroup = claimGroups[3],
          chipLoadingErrorMessage = if (hasError) "" else null,
          isLoading = false,
        ),
        loadClaimGroups = {},
        onSelectClaimGroup = {},
        onContinue = {},
        showedStartClaimError = {},
        navigateUp = {},
      )
    }
  }
}
