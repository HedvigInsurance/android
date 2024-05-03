package com.hedvig.android.feature.claimtriaging.claimgroups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.scaffold.ClaimFlowScaffold
import com.hedvig.android.core.ui.text.WarningTextWithIcon
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimtriaging.ClaimGroup
import com.hedvig.android.data.claimtriaging.ClaimGroupId
import com.hedvig.android.feature.claimtriaging.OptionChipsFlowRow
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun ClaimGroupsDestination(
  viewModel: ClaimGroupsViewModel,
  windowSizeClass: WindowSizeClass,
  onClaimGroupWithEntryPointsSubmit: (ClaimGroup) -> Unit,
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
  ClaimGroupsScreen(
    uiState = uiState,
    loadClaimGroups = viewModel::loadClaimGroups,
    onSelectClaimGroup = viewModel::onSelectClaimGroup,
    onContinue = {
      val selectedClaimGroup = uiState.selectedClaimGroup
      if (selectedClaimGroup != null) {
        if (selectedClaimGroup.entryPoints.isEmpty()) {
          viewModel.startClaimFlow()
        } else {
          onClaimGroupWithEntryPointsSubmit(selectedClaimGroup)
        }
      } else {
        viewModel.continueWithoutSelection()
      }
    },
    showedStartClaimError = viewModel::showedStartClaimError,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
    windowSizeClass = windowSizeClass,
  )
}

@Composable
private fun ClaimGroupsScreen(
  uiState: ClaimGroupsUiState,
  windowSizeClass: WindowSizeClass,
  loadClaimGroups: () -> Unit,
  onSelectClaimGroup: (claimGroup: ClaimGroup) -> Unit,
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

  ClaimFlowScaffold(
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxWidth(),
    windowSizeClass = windowSizeClass,
    closeClaimFlow = closeClaimFlow,
  ) {
    Spacer(Modifier.height(16.dp))
    if (uiState.chipLoadingErrorMessage != null) {
      HedvigErrorSection(onButtonClick = loadClaimGroups)
    } else {
      Text(
        text = stringResource(R.string.CLAIM_TRIAGING_NAVIGATION_TITLE),
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
        items = uiState.claimGroups,
        itemDisplayName = ClaimGroup::displayName,
        selectedItem = uiState.selectedClaimGroup,
        onItemClick = { claimGroup -> onSelectClaimGroup(claimGroup) },
        modifier = Modifier.padding(horizontal = 16.dp),
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
      Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimGroupsScreen(
  @PreviewParameter(
    com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider::class,
  ) hasError: Boolean,
) {
  HedvigTheme {
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
          haveTriedContinuingWithoutSelection = true,
          chipLoadingErrorMessage = if (hasError) "" else null,
          isLoading = false,
        ),
        loadClaimGroups = {},
        onSelectClaimGroup = {},
        onContinue = {},
        showedStartClaimError = {},
        navigateUp = {},
        closeClaimFlow = {},
        windowSizeClass = WindowSizeClass.calculateForPreview(),
      )
    }
  }
}
