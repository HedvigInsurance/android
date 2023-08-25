package com.hedvig.app.feature.embark.passages.addressautocomplete.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun AddressCard(
  addressText: Pair<String, String?>?,
  placeholderText: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
  ) {
    AddressTextColumn(
      addressText = addressText,
      placeholderText = placeholderText,
      modifier = Modifier.padding(24.dp),
    )
  }
}

@Composable
private fun AddressTextColumn(
  addressText: Pair<String, String?>?,
  placeholderText: String,
  modifier: Modifier = Modifier,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier,
  ) {
    if (addressText == null) {
      Text(
        placeholderText,
        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        textAlign = TextAlign.Center,
      )
    } else {
      Text(
        addressText.first,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
      )
      addressText.second?.let { secondaryText ->
        if (secondaryText.isBlank()) return@let
        Text(
          secondaryText,
          style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewAddressCard() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AddressCard(
        "Willemoesgade 4, st. tv".repeat(3) to "2100 København Ø".repeat(1),
        "",
        {},
      )
    }
  }
}
