package com.hedvig.android.audio.player.internal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.onWarningContainer
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.material3.warningContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.WarningFilled

@Composable
internal fun FailedAudioPlayerCard(tryAgain: () -> Unit, modifier: Modifier = Modifier) {
  Column(modifier.clip(MaterialTheme.shapes.squircleMedium)) {
    Spacer(Modifier.height(20.dp))
    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.Top,
      modifier = Modifier.padding(horizontal = 16.dp),
    ) {
      Icon(
        imageVector = Icons.Hedvig.WarningFilled,
        contentDescription = null,
        modifier = Modifier.padding(top = 4.dp),
      )
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = stringResource(hedvig.resources.R.string.claim_status_detail_info_error_title),
          style = MaterialTheme.typography.bodyLarge,
        )
        Text(
          text = stringResource(hedvig.resources.R.string.claim_status_detail_info_error_body),
          style = MaterialTheme.typography.bodyMedium.copy(
            color = LocalContentColor.current.copy(alpha = 0.7f),
          ),
        )
      }
    }
    Spacer(Modifier.height(16.dp))
    HorizontalDivider()
    TextButton(
      onClick = tryAgain,
      modifier = Modifier
        .align(Alignment.End)
        .padding(horizontal = 8.dp, vertical = 5.dp),
      colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current),
    ) {
      Text(
        text = stringResource(hedvig.resources.R.string.claim_status_detail_info_error_button),
        style = MaterialTheme.typography.bodyMedium,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewFailedAudioPlayerCard() {
  HedvigTheme {
    Surface(
      color = MaterialTheme.colorScheme.warningContainer,
      contentColor = MaterialTheme.colorScheme.onWarningContainer,
      modifier = Modifier.padding(16.dp),
    ) {
      FailedAudioPlayerCard({})
    }
  }
}
