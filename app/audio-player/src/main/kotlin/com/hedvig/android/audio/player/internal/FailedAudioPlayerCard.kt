package com.hedvig.android.audio.player.internal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.onWarning
import com.hedvig.android.core.designsystem.theme.warning

@Composable
internal fun FailedAudioPlayerCard(
  tryAgain: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Spacer(Modifier.height(20.dp))
    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.Top,
      modifier = Modifier.padding(horizontal = 16.dp),
    ) {
      Icon(
        painter = painterResource(com.hedvig.android.core.designsystem.R.drawable.ic_warning_triangle),
        contentDescription = null,
      )
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = stringResource(hedvig.resources.R.string.claim_status_detail_info_error_title),
          style = MaterialTheme.typography.body1,
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
          Text(
            text = stringResource(hedvig.resources.R.string.claim_status_detail_info_error_body),
            style = MaterialTheme.typography.body2,
          )
        }
      }
    }
    Spacer(Modifier.height(16.dp))
    Divider()
    TextButton(
      onClick = tryAgain,
      modifier = Modifier
        .align(Alignment.End)
        .padding(horizontal = 8.dp, vertical = 5.dp),
      colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current),
    ) {
      Text(
        text = stringResource(hedvig.resources.R.string.claim_status_detail_info_error_button),
        style = MaterialTheme.typography.body2,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewFailedAudioPlayerCard() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.warning) {
      CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.onWarning) {
        FailedAudioPlayerCard({})
      }
    }
  }
}
