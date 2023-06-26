package com.hedvig.android.feature.legacyclaimtriaging

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.core.ui.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.data.claimtriaging.EntryPoint
import com.hedvig.android.data.claimtriaging.EntryPointId
import kotlinx.collections.immutable.persistentListOf
import java.util.UUID

@Composable
internal fun LegacyClaimTriagingDestination(
  viewModel: LegacyClaimTriagingViewModel,
  startClaimFlow: (entryPointId: EntryPointId?) -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val entryPointId = uiState.selectedClaim?.id
  LaunchedEffect(entryPointId) {
    if (entryPointId != null) {
      startClaimFlow(entryPointId)
    }
  }
  LegacyClaimTriagingScreen(
    uiState = uiState,
    retryLoadSearchableClaims = viewModel::loadSearchableClaims,
    onSelectClaim = viewModel::onSelectClaim,
    navigateUp = navigateUp,
  )
}

@Composable
private fun LegacyClaimTriagingScreen(
  uiState: LegacyClaimTriagingUiState,
  retryLoadSearchableClaims: () -> Unit,
  onSelectClaim: (EntryPoint) -> Unit,
  navigateUp: () -> Unit,
) {
  Box(
    modifier = Modifier.fillMaxSize(),
    propagateMinConstraints = true,
  ) {
    HedvigScaffold(
      navigateUp = navigateUp,
    ) {
      Spacer(Modifier.height(16.dp))
      Text(
        text = stringResource(hedvig.resources.R.string.CLAIM_TRIAGING_TITLE),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
      )
      Spacer(Modifier.height(24.dp))
      val errorMessage = uiState.errorMessage
      if (errorMessage != null) {
        GenericErrorScreen(
          onRetryButtonClick = retryLoadSearchableClaims,
          modifier = Modifier.padding(16.dp),
        )
      } else {
        CommonClaims(
          selectClaim = onSelectClaim,
          commonClaims = uiState.commonClaims,
        )
      }
      Spacer(Modifier.height(24.dp))
    }
    if (uiState.isLoading) {
      HedvigFullScreenCenterAlignedProgress()
    }
  }
}

@Composable
private fun CommonClaims(
  selectClaim: (EntryPoint) -> Unit,
  commonClaims: List<EntryPoint>,
) {
  Surface(
    modifier = Modifier.padding(horizontal = 24.dp),
    shape = MaterialTheme.shapes.medium,
  ) {
    Column {
      commonClaims.forEachIndexed { index, claim ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectClaim(claim) }
            .padding(24.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(claim.displayName, Modifier.align(Alignment.CenterVertically))
          Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Arrow",
          )
        }
        if (index < commonClaims.lastIndex) {
          Divider(Modifier.padding(horizontal = 8.dp))
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewLegacyClaimTriagingScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      LegacyClaimTriagingScreen(
        uiState = LegacyClaimTriagingUiState(
          commonClaims = List(10) {
            EntryPoint(
              id = EntryPointId(UUID.randomUUID().toString()),
              displayName = "Broken phone #$it",
              entryPointOptions = persistentListOf(),
            )
          },
          results = emptyList(),
          selectedClaim = null,
          errorMessage = null,
          isLoading = false,
        ),
        retryLoadSearchableClaims = {},
        onSelectClaim = {},
        navigateUp = {},
      )
    }
  }
}
