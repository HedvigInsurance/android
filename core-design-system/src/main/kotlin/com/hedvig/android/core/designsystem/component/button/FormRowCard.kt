package com.hedvig.android.core.designsystem.component.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun FormRowCard(
  modifier: Modifier = Modifier,
  content: @Composable RowScope.() -> Unit,
) {
  HedvigCard(modifier = modifier.heightIn(min = 64.dp)) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
      content()
    }
  }
}

@Composable
fun FormRowCard(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  enabled: Boolean = true,
  content: @Composable RowScope.() -> Unit,
) {
  HedvigCard(
    modifier = modifier.heightIn(min = 64.dp),
    enabled = enabled,
    onClick = onClick,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
      content()
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewFormRowCard() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      FormRowCard {
        Text("Date of Incident")
        Spacer(Modifier.weight(1f))
        Text("2023-03-14")
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewMultipleFormRowCards() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      @Composable
      fun FormRowCard() {
        FormRowCard {
          Text("Date of Incident")
          Spacer(Modifier.weight(1f))
          Text("2023-03-14")
        }
      }
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(12.dp),
      ) {
        repeat(4) {
          FormRowCard()
        }
      }
    }
  }
}
