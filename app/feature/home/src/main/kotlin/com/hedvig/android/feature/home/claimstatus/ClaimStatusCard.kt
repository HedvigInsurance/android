package com.hedvig.android.feature.home.claimstatus

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
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.home.claimdetail.ui.previewList
import com.hedvig.android.feature.home.claimstatus.claimprogress.ClaimProgressRow
import com.hedvig.android.feature.home.claimstatus.claimprogress.ClaimProgressUiState
import com.hedvig.android.feature.home.claimstatus.data.ClaimStatusCardUiState
import com.hedvig.android.feature.home.claimstatus.data.PillUiState
import java.util.UUID

@Composable
internal fun ClaimStatusCard(
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
    shape = MaterialTheme.shapes.squircle,
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
