package com.hedvig.android.feature.home.claimdetail.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.home.claimdetail.model.ClaimDetailCardUiState
import com.hedvig.android.feature.home.claimstatus.claimprogress.ClaimProgressUiState

@Composable
internal fun ClaimInfoCard(
  uiState: ClaimDetailCardUiState,
  onChatClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(modifier = modifier) {
    Column {
      TopSection(
        statusParagraph = uiState.statusParagraph,
        modifier = Modifier
          .animateContentSize()
          .padding(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 20.dp,
          ),
      )
      Divider()
      BottomSection(
        onChatClick = onChatClick,
        modifier = Modifier.padding(16.dp),
      )
    }
  }
}

@Composable
private fun TopSection(
  statusParagraph: String,
  modifier: Modifier = Modifier,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(24.dp),
    modifier = modifier,
  ) {
    Text(
      text = statusParagraph,
      style = MaterialTheme.typography.bodyLarge,
    )
  }
}

@Composable
private fun BottomSection(
  onChatClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(
        text = stringResource(hedvig.resources.R.string.claim_status_contact_generic_subtitle),
        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
      )
      Text(
        text = stringResource(hedvig.resources.R.string.claim_status_contact_generic_title),
        style = MaterialTheme.typography.bodyLarge,
      )
    }
    ChatIcon(
      onClick = onChatClick,
      contentDescription = stringResource(hedvig.resources.R.string.claim_status_detail_chat_button_description),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimDetailCard() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ClaimInfoCard(
        ClaimDetailCardUiState(
          claimProgressItemsUiState = ClaimProgressUiState.previewList(),
          statusParagraph = "StatusParagraph",
        ),
        {},
      )
    }
  }
}
