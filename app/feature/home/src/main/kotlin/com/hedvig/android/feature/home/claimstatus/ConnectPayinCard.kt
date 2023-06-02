package com.hedvig.android.feature.home.claimstatus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.hedvigContentColorFor
import com.hedvig.android.core.designsystem.theme.warning

@Composable
internal fun ConnectPayinCard(
  onActionClick: () -> Unit,
  onShown: () -> Unit,
  modifier: Modifier = Modifier,
) {
  LaunchedEffect(Unit) {
    onShown()
  }

  val colorWarning = MaterialTheme.colors.warning
  Card(
    modifier = modifier
      .padding(16.dp)
      .fillMaxWidth(),
    backgroundColor = colorWarning,
    contentColor = hedvigContentColorFor(colorWarning),
  ) {
    Column {
      Spacer(Modifier.height(16.dp))
      Row(
        modifier = Modifier.padding(horizontal = 16.dp),
      ) {
        Icon(
          painter = painterResource(com.hedvig.android.core.designsystem.R.drawable.ic_warning_triangle),
          contentDescription = null,
        )
        Spacer(Modifier.width(16.dp))
        Column {
          Text(
            text = stringResource(hedvig.resources.R.string.info_card_missing_payment_title),
            style = MaterialTheme.typography.subtitle1,
          )
          Spacer(Modifier.height(8.dp))
          Text(
            text = stringResource(hedvig.resources.R.string.info_card_missing_payment_body),
            style = MaterialTheme.typography.body2,
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      Divider()
      TextButton(
        onClick = onActionClick,
        modifier = Modifier
          .padding(vertical = 4.dp)
          .padding(end = 8.dp)
          .align(Alignment.End),
        colors = ButtonDefaults.textButtonColors(
          contentColor = hedvigContentColorFor(colorWarning),
        ),
      ) {
        Text(
          text = stringResource(hedvig.resources.R.string.info_card_missing_payment_button_text),
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewConnectPayinCard() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      ConnectPayinCard(
        onShown = {},
        onActionClick = {},
      )
    }
  }
}
