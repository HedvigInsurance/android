package com.hedvig.app.feature.home.ui.claimstatus.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.card.HedvigCardElevation
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusCardUiState
import com.hedvig.app.feature.home.ui.claimstatus.data.PillUiState
import com.hedvig.app.ui.compose.composables.claimprogress.ClaimProgressRow
import com.hedvig.app.ui.compose.composables.claimprogress.ClaimProgressUiState
import com.hedvig.app.util.compose.preview.previewList
import java.util.UUID

@Composable
fun ClaimStatusCard(
  uiState: ClaimStatusCardUiState,
  goToDetailScreen: ((claimId: String) -> Unit)?,
  modifier: Modifier = Modifier,
  onClaimCardShown: (String) -> Unit,
) {
  LaunchedEffect(uiState.id) {
    onClaimCardShown(uiState.id)
  }
  val onClick: (() -> Unit)? = if (goToDetailScreen != null) {
    { goToDetailScreen.invoke(uiState.id) }
  } else {
    null
  }
  HedvigCard(
    onClick = onClick,
    colors = CardDefaults.elevatedCardColors(),
    elevation = HedvigCardElevation.Elevated(4.dp),
    modifier = modifier,
  ) {
    Column {
      TopInfo(
        pillsUiState = uiState.pillsUiState,
        title = uiState.title,
        subtitle = uiState.subtitle,
        isClickable = goToDetailScreen != null,
        modifier = Modifier.padding(16.dp),
      )
      Divider()
      ClaimProgressRow(
        claimProgressItemsUiState = uiState.claimProgressItemsUiState,
        modifier = Modifier.padding(16.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimStatusCard() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      val claimStatusData = ClaimStatusCardUiState(
        id = UUID.randomUUID().toString(),
        pillsUiState = PillUiState.previewList(),
        title = "All-risk",
        subtitle = "Home Insurance Renter",
        claimProgressItemsUiState = ClaimProgressUiState.previewList(),
      )
      ClaimStatusCard(claimStatusData, null) {}
    }
  }
}
